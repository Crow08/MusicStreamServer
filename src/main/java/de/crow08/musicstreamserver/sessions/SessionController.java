package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.model.media.Media;
import de.crow08.musicstreamserver.model.queue.Queue;
import de.crow08.musicstreamserver.model.users.User;
import de.crow08.musicstreamserver.model.users.UserRepository;
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
 * Controller for sessions to manage session states and organize queue, history and other data while the session is
 * being worked with. User command should be working with this controller to modify data instead of accessing the
 * session directly.
 */
@Controller
public class SessionController {

  private final static long SYNC_DELAY = 200;

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

  private static boolean isInSyncState(Session session) {
    Session.SessionState currentState = session.getSessionState();
    return currentState.equals(Session.SessionState.SYNC_PLAY) ||
        currentState.equals(Session.SessionState.SYNC_PAUSE) ||
        currentState.equals(Session.SessionState.SYNC_STOP);
  }

  private void setUpListeners() {
    webSocketSessionController.addDisconnectListener(userId -> {
      for (Session session : sessionRepository.findAll()) {
        Optional<User> disconnectedUser = session.getUsers().stream().filter(user -> user.getId() == userId).findFirst();
        disconnectedUser.ifPresent(user -> {
          this.removeUserFromSession(user, session);
          // this message has to be fired here to inform other Users about an unexpected disconnect
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
   * Gets the current media being played. If no media is being played {@link #nextMedia(Session)} is called
   * automatically to move an element in the current position.
   *
   * @param session to get the media for.
   * @return the current media.
   */
  public Optional<Media> getCurrentMedia(Session session) {
    if (session.getQueue().getCurrentMedia() == null) {
      this.nextMedia(session);
    }
    return Optional.ofNullable(session.getQueue().getCurrentMedia());
  }

  /**
   * Gets the time the media has been played for. This time excludes pauses.
   * If the media is currently playing this represents a snapshot of the time at the moment of execution.
   *
   * @param session the session of the media to get the offset for.
   * @return the duration of the elapsed time of the current media.
   */
  public Duration getMediaStartOffset(Session session) {
    Duration duration = Duration.ZERO;
    if (session.getMediaStarted() != null) {
      duration = duration.plus(Duration.between(session.getMediaStarted(), Instant.now()));
    }
    if (duration != null) {
      duration = duration.plus(session.getSavedProgression());
    }
    return duration;
  }

  /**
   * Moves the current media play 0position by the defined offset and pauses the playback at the new position.
   *
   * @param session the session of the media to move the offset for.
   * @param offset  the amount in ms by which the current offset should be moved.
   * @return new current media offset see {@link #getMediaStartOffset(Session)}
   */
  public Duration moveMediaStartOffset(Session session, long offset) {
    // set saved progression to current progression
    Instant now = Instant.now();
    if (session.getMediaStarted() != null) {
      session.setSavedProgression(session.getSavedProgression().plus(Duration.between(session.getMediaStarted(), now)));
    }
    session.setMediaStarted(null);
    session.setSavedProgression(session.getSavedProgression().plus(offset, ChronoUnit.MILLIS));
    return this.getMediaStartOffset(session);
  }

  /**
   * This is a macro function which tries to move the next media in the queue to the current media while
   * adding the old current media to the history.
   *
   * @param session for with to move to the next media
   */
  public void nextMedia(Session session) {
    Queue queue = session.getQueue();
    session.setMediaStarted(null);
    session.setSavedProgression(Duration.ZERO);
    Media currentMedia = queue.getCurrentMedia();
    // Add current media to history
    if (currentMedia != null) {
      queue.getHistoryMedia().add(currentMedia);
    }
    // If queue is empty and loopMode is active, dump history into queue.
    if (queue.getQueuedMedia().size() == 0 && session.isLoopMode() && !queue.getHistoryMedia().isEmpty()) {
      queue.getQueuedMedia().addAll(queue.getHistoryMedia());
      queue.getHistoryMedia().clear();
    }
    // Get current media from queue.
    if (queue.getQueuedMedia().size() > 0) {
      queue.setCurrentMedia(queue.getQueuedMedia().get(0));
      queue.getQueuedMedia().remove(0);
    } else {
      queue.setCurrentMedia(null);
    }
  }

  /**
   * This is a macro function which tries to place the latest media in the history in the current position.
   * The currently played media will be pushed on top of the queue.
   *
   * @param session for with to move to the next media
   */
  public void previousMedia(Session session) {
    Queue queue = session.getQueue();
    session.setMediaStarted(null);
    session.setSavedProgression(Duration.ZERO);
    Media currentMedia = queue.getCurrentMedia();
    if (currentMedia != null) {
      queue.getQueuedMedia().add(0, currentMedia);
    }
    // if history is empty and loop mode is active use the last media int the queue instead.
    if (queue.getHistoryMedia().size() == 0 && session.isLoopMode() && !queue.getQueuedMedia().isEmpty()) {
      queue.setCurrentMedia(queue.getQueuedMedia().get(queue.getQueuedMedia().size() - 1));
      queue.getQueuedMedia().remove(queue.getQueuedMedia().size() - 1);
      // if the history is not empty use the latest long.
    } else if (queue.getHistoryMedia().size() > 0) {
      queue.setCurrentMedia(queue.getHistoryMedia().get(queue.getHistoryMedia().size() - 1));
      queue.getHistoryMedia().remove(queue.getHistoryMedia().size() - 1);
    } else {
      queue.setCurrentMedia(null);
    }
  }

  /**
   * Adds a list of media to the end of the queue of the session
   *
   * @param session for the media to be added to
   * @param media   list of new media
   */

  public void addMedia(Session session, List<Media> media) {
    session.getQueue().getQueuedMedia().addAll(media);
  }

  /**
   * Marks the start of a media. This resets the time played of the previous media.
   *
   * @param session for which the media starts playing.
   */
  public void start(Session session) {
    session.setMediaStarted(null);
    session.setSavedProgression(Duration.ZERO);
    if (isInSyncState(session)) {
      session.setSessionState(Session.SessionState.SYNC_PLAY);
    } else {
      session.setSessionState(Session.SessionState.PLAY);
    }
  }

  /**
   * Marks the media as paused and saves the progression time since the last resume or start of this media.
   * the current start time will be set to null.
   *
   * @param session for which the media is being paused.
   * @return the total accumulated time across all pauses and resumes this media has been played for.
   */
  public Duration pause(Session session) {
    Instant now = Instant.now();
    if (session.getMediaStarted() != null) {
      session.setSavedProgression(session.getSavedProgression().plus(Duration.between(session.getMediaStarted(), now)));
    }
    session.setMediaStarted(null);
    session.setSessionState(Session.SessionState.PAUSE);
    return this.getMediaStartOffset(session);
  }

  /**
   * Marks this media as resumed and defines a new start time.
   * The media is planned to start within the given {@value #SYNC_DELAY}.
   *
   * @param session for which the media is being resumed.
   * @return server time of the media start.
   */
  public Instant resume(Session session) {
    if (isInSyncState(session)) {
      session.setSessionState(Session.SessionState.SYNC_PLAY);
      return null;
    } else {
      Instant now = Instant.now();
      session.setMediaStarted(now.plus(SYNC_DELAY, ChronoUnit.MILLIS));
      session.setSessionState(Session.SessionState.PLAY);
      return session.getMediaStarted();
    }
  }

  /**
   * Marks the current media as stopped deleting all process and the start time.
   *
   * @param session for which the media is being stopped.
   */
  public void stop(Session session) {
    session.setSavedProgression(Duration.ZERO);
    session.setMediaStarted(null);
    session.setSessionState(Session.SessionState.STOP);
  }

  /**
   * Shuffles the current Queue.
   *
   * @param session for which the queue is shuffled for.
   */
  public void shuffleQueue(Session session) {
    Collections.shuffle(session.getQueue().getQueuedMedia());
  }

  /**
   * Deletes media from the current queue.
   *
   * @param session    to delete from.
   * @param queueIndex to delete.
   */
  public void deleteMediaFromQueue(Session session, int queueIndex) {
    session.getQueue().getQueuedMedia().remove(queueIndex);
  }

  /**
   * Deletes media from the current history.
   *
   * @param session      to delete from.
   * @param historyIndex to delete.
   */
  public void deleteMediaFromHistory(Session session, int historyIndex) {
    session.getQueue().getHistoryMedia().remove(historyIndex);
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

  /**
   * Coordinates Users to perform a synchronized start. This method has to be called for every user once to mark the as
   * ready for the start.
   * When the last user is the session calls this method is returns true and a synchronized start can be triggered.
   *
   * @param user     User who indicates its ready state
   * @param session  session the user is in.
   * @param mediaId  Id of the media the user marks its readiness for.
   * @param duration timestamp in the media the user marks its readiness for.
   * @return true if the last user is ready, false otherwise.
   */
  synchronized public boolean userIsReady(User user, Session session, long mediaId, long duration) {
    SessionUserSynchronization sync = session.getSessionUserSynchronization();
    sync.addSyncUsers(user.getId(), mediaId, duration);
    if (session.getUsers().size() == sync.getSyncUsers().size()) {
      sync.completeSync();
      return true;
    }
    return false;
  }

  public void enterSyncState(Session session) {
    switch (session.getSessionState()) {
      case PLAY -> session.setSessionState(Session.SessionState.SYNC_PLAY);
      case PAUSE -> session.setSessionState(Session.SessionState.SYNC_PAUSE);
      case STOP -> session.setSessionState(Session.SessionState.SYNC_STOP);
    }
  }

  public void leaveSyncState(Session session) {
    switch (session.getSessionState()) {
      case SYNC_PLAY -> session.setSessionState(Session.SessionState.PLAY);
      case SYNC_PAUSE -> session.setSessionState(Session.SessionState.PAUSE);
      case SYNC_STOP -> session.setSessionState(Session.SessionState.STOP);
    }
  }
}
