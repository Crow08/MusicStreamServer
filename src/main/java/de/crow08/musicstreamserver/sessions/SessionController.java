package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.queue.Queue;
import de.crow08.musicstreamserver.song.Song;
import de.crow08.musicstreamserver.users.User;
import de.crow08.musicstreamserver.users.UserRepository;
import de.crow08.musicstreamserver.wscommunication.WebSocketSessionController;
import de.crow08.musicstreamserver.wscommunication.commands.LeaveCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Controller for sessions to manage session states and move songs and other data while the session is being worked with.
 * User command should be working with this controller to modify data instead of accessing the session directly.
 */
@Controller
public class SessionController {

  public final static long SYNC_DELAY = 1000;

  private final WebSocketSessionController webSocketSessionController;

  private final SimpMessagingTemplate simpMessagingTemplate;

  private final UserRepository userRepository;
  private final SessionRepository sessionRepository;

  @Autowired
  public SessionController(final WebSocketSessionController webSocketSessionController,
                           SimpMessagingTemplate simpMessagingTemplate,
                           UserRepository userRepository,
                           SessionRepository sessionRepository) {
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.webSocketSessionController = webSocketSessionController;
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
    setUpListeners();

  }

  private void setUpListeners() {
    webSocketSessionController.addDisconnectListener(userId -> {
      for (Session session : sessionRepository.findAll()) {
        Optional<User> disconnectedUser = session.getUsers().stream().filter(user -> user.getId() == userId).findFirst();
        disconnectedUser.ifPresent(user -> {
          this.removeUserFromSession(user, session);
          // this messgae has to be fired here to inform other Memoers about an unexpected disconnect
          simpMessagingTemplate.convertAndSend("/topic/sessions/" + session.getId(), new LeaveCommand(userId));
        });
      }
    });
    webSocketSessionController.addConnectListener((userId, sessionId) -> {
      Optional<Session> session = getSessionsAsStream().filter(ses -> sessionId == ses.getId()).findFirst();
      if (session.isPresent()) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(value -> {
          this.addUserToSession(user.get(), session.get());
        });

      }
    });
  }

  private Stream<Session> getSessionsAsStream() {
    return StreamSupport.stream(sessionRepository.findAll().spliterator(), false);
  }

  /**
   * Gets the current song being played. If no song is being played {@link #nextSong(Session)} is called
   * automatically to move a song in the current position.
   *
   * @param session to get the song for.
   * @return the current song.
   */
  public Optional<Song> getCurrentSong(Session session) {
    if (session.getQueue().getCurrentSong() == null) {
      this.nextSong(session);
    }
    return Optional.ofNullable(session.getQueue().getCurrentSong());
  }

  /**
   * Gets the time the song has been played for. This time excludes pauses.
   * If the song is currently playing this represents a snapshot of the time at the moment of execution.
   *
   * @param session the session of the song to get the offset for.
   * @return the duration of the elapsed time of the current song.
   */
  public Duration getSongStartOffset(Session session) {
    Duration duration = Duration.ZERO;
    if (session.getSongStarted() != null) {
      duration = duration.plus(Duration.between(session.getSongStarted(), Instant.now()));
    }
    if (duration != null) {
      duration = duration.plus(session.getSavedProgression());
    }
    return duration;
  }

  /**
   * This is a macro function which tries to move the next song in the queue to the current song while
   * adding the old current song to the history.
   *
   * @param session for with to move to the next song
   */
  public void nextSong(Session session) {
    Queue queue = session.getQueue();
    Song currentSong = queue.getCurrentSong();
    // Add current song to history
    if (currentSong != null) {
      queue.getHistorySongs().add(currentSong);
    }
    // If queue is empty and loopMode is active, dump history into queue.
    if (queue.getQueuedSongs().size() == 0 && session.isLoopMode() && !queue.getHistorySongs().isEmpty()) {
      queue.getQueuedSongs().addAll(queue.getHistorySongs());
      queue.getHistorySongs().clear();
    }
    // Get current song from queue.
    if (queue.getQueuedSongs().size() > 0) {
      queue.setCurrentSong(queue.getQueuedSongs().get(0));
      queue.getQueuedSongs().remove(0);
    } else {
      queue.setCurrentSong(null);
    }
  }

  /**
   * This is a macro function which tries to place the latest song in the history in the current position.
   * The currently played song will be pushed on top of the queue.
   *
   * @param session for with to move to the next song
   */
  public void previousSong(Session session) {
    Queue queue = session.getQueue();
    Song currentSong = queue.getCurrentSong();
    if (currentSong != null) {
      queue.getQueuedSongs().add(0, currentSong);
    }
    // if history is empty and loop mode is active use the last song int the queue instead.
    if (queue.getHistorySongs().size() == 0 && session.isLoopMode() && !queue.getQueuedSongs().isEmpty()) {
      queue.setCurrentSong(queue.getQueuedSongs().get(queue.getQueuedSongs().size() - 1));
      queue.getQueuedSongs().remove(queue.getQueuedSongs().size() - 1);
      // if the history is not empty use the latest long.
    } else if (queue.getHistorySongs().size() > 0) {
      queue.setCurrentSong(queue.getHistorySongs().get(queue.getHistorySongs().size() - 1));
      queue.getHistorySongs().remove(queue.getHistorySongs().size() - 1);
    } else {
      queue.setCurrentSong(null);
    }
  }

  /**
   * Adds a list of songs to the end of the queue of the session
   *
   * @param session for the songs to be added to
   * @param songs   list of new songs
   */

  public void addSongs(Session session, List<Song> songs) {
    session.getQueue().getQueuedSongs().addAll(songs);
  }

  /**
   * Marks the start of a song. This resets the time played of the previous song.
   * The Song is planned to start within the given {@value #SYNC_DELAY}.
   *
   * @param session for which the song starts playing.
   * @return server time of the song start.
   */
  public Instant start(Session session) {
    session.setSongStarted(Instant.now().plus(SYNC_DELAY, ChronoUnit.MILLIS));
    session.setSavedProgression(Duration.ZERO);
    session.setSessionState(Session.SessionState.PLAY);
    return session.getSongStarted();
  }

  /**
   * Marks the song as paused and saves the progression time since the last resume or start of this song.
   * the current start time will be set to null.
   *
   * @param session for which the song is being paused.
   * @return the total accumulated time across all pauses and resumes this song has been played for.
   */
  public Duration pause(Session session) {
    Instant now = Instant.now();
    if (session.getSongStarted() != null) {
      session.setSavedProgression(session.getSavedProgression().plus(Duration.between(session.getSongStarted(), now)));
    }
    session.setSongStarted(null);
    session.setSessionState(Session.SessionState.PAUSE);
    return this.getSongStartOffset(session);
  }

  /**
   * Marks this song as resumed and defines a new start time.
   * The Song is planned to start within the given {@value #SYNC_DELAY}.
   *
   * @param session for which the song is being resumed.
   * @return server time of the song start.
   */
  public Instant resume(Session session) {
    Instant now = Instant.now();
    session.setSongStarted(now.plus(SYNC_DELAY, ChronoUnit.MILLIS));
    session.setSessionState(Session.SessionState.PLAY);
    return session.getSongStarted();
  }

  /**
   * Marks the current Song as stopped deleting all process and the start time.
   *
   * @param session for which the song is being stopped.
   */
  public void stop(Session session) {
    session.setSavedProgression(Duration.ZERO);
    session.setSongStarted(null);
    session.setSessionState(Session.SessionState.STOP);
  }

  /**
   * Shuffles the current Queue.
   *
   * @param session for which the queue is shuffled for.
   */
  public void shuffleQueue(Session session) {
    Collections.shuffle(session.getQueue().getQueuedSongs());
  }

  /**
   * Deletes song from the current queue.
   *
   * @param session    to delete from.
   * @param queueIndex to delete.
   */
  public void deleteSongFromQueue(Session session, int queueIndex) {
    session.getQueue().getQueuedSongs().remove(queueIndex);
  }

  /**
   * Deletes song from the current history.
   *
   * @param session      to delete from.
   * @param historyIndex to delete.
   */
  public void deleteSongFromHistory(Session session, int historyIndex) {
    session.getQueue().getHistorySongs().remove(historyIndex);
  }

  public Session createNewSession(String name) {
    Session session = new Session(name);
    sessionRepository.save(session);
    return session;
  }

  public void addUserToSession(User newUser, Session session) {
    if (session.getUsers().stream().noneMatch(user -> user.getId() == newUser.getId())) {
      session.getUsers().add(newUser);
      sessionRepository.save(session);
    }
  }

  public void removeUserFromSession(User oldUser, Session session) {
    session.getUsers().remove(oldUser);
    sessionRepository.save(session);
  }
}
