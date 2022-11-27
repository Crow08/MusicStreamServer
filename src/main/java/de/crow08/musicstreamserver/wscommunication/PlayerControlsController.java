package de.crow08.musicstreamserver.wscommunication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.crow08.musicstreamserver.model.media.MediaType;
import de.crow08.musicstreamserver.model.queue.Queue;
import de.crow08.musicstreamserver.sessions.Session;
import de.crow08.musicstreamserver.sessions.SessionController;
import de.crow08.musicstreamserver.sessions.SessionRepository;
import de.crow08.musicstreamserver.model.media.Media;
import de.crow08.musicstreamserver.model.media.MinimalMedia;
import de.crow08.musicstreamserver.model.users.User;
import de.crow08.musicstreamserver.model.users.UserRepository;
import de.crow08.musicstreamserver.wscommunication.commands.Command;
import de.crow08.musicstreamserver.wscommunication.commands.JoinCommand;
import de.crow08.musicstreamserver.wscommunication.commands.JumpCommand;
import de.crow08.musicstreamserver.wscommunication.commands.LeaveCommand;
import de.crow08.musicstreamserver.wscommunication.commands.NopCommand;
import de.crow08.musicstreamserver.wscommunication.commands.PauseCommand;
import de.crow08.musicstreamserver.wscommunication.commands.ResumeCommand;
import de.crow08.musicstreamserver.wscommunication.commands.StartCommand;
import de.crow08.musicstreamserver.wscommunication.commands.StopCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateHistoryCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateLoopModeCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateQueueAndHistoryCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateQueueCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PlayerControlsController {

  private final SessionRepository sessionRepository;
  private final SessionController sessionController;
  private final UserRepository userRepository;

  @Autowired
  public PlayerControlsController(SessionRepository sessionRepository,
                                  SessionController sessionController,
                                  UserRepository userRepository) {
    this.sessionRepository = sessionRepository;
    this.sessionController = sessionController;
    this.userRepository = userRepository;
  }

  @MessageMapping("/sessions/{sessionId}/commands/start")
  @SendTo("/topic/sessions/{sessionId}")
  public Command start(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    return startSong(session);
  }

  @MessageMapping("/sessions/{sessionId}/commands/jump/{offset}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command jump(@DestinationVariable long sessionId, @DestinationVariable long offset, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.setSongStartOffset(session, offset);
    long startTime = Instant.now().plus(SessionController.SYNC_DELAY, ChronoUnit.MILLIS).toEpochMilli();
    long startOffset = sessionController.getSongStartOffset(session).plus(SessionController.SYNC_DELAY, ChronoUnit.MILLIS).toMillis();

    return new JumpCommand(startTime, startOffset);
  }

  @MessageMapping("/sessions/{sessionId}/commands/skip")
  @SendTo("/topic/sessions/{sessionId}")
  public Command skip(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.nextSong(session);
    return startSong(session);
  }

  @MessageMapping("/sessions/{sessionId}/commands/previous")
  @SendTo("/topic/sessions/{sessionId}")
  public Command previous(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.previousSong(session);
    return startSong(session);
  }

  @MessageMapping("/sessions/{sessionId}/commands/pause")
  @SendTo("/topic/sessions/{sessionId}")
  public Command pause(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    long pausePosition = sessionController.pause(session).toMillis();
    return new PauseCommand(pausePosition);
  }

  @MessageMapping("/sessions/{sessionId}/commands/resume")
  @SendTo("/topic/sessions/{sessionId}")
  public Command resume(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    long startTime = sessionController.resume(session).toEpochMilli();
    return new ResumeCommand(startTime);
  }

  @MessageMapping("/sessions/{sessionId}/commands/stop")
  @SendTo("/topic/sessions/{sessionId}")
  public Command stop(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.stop(session);
    return new StopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/join/{userId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command join(@DestinationVariable long sessionId, @DestinationVariable long userId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    Optional<Media> currentSong = sessionController.getCurrentSong(session);
    MinimalMedia minSong = null;
    if (currentSong.isPresent()) {
      minSong = new MinimalMedia(currentSong.get().getId(), currentSong.get().getTitle(), currentSong.get().getType());
    }
    long startTime = Instant.now().plus(SessionController.SYNC_DELAY, ChronoUnit.MILLIS).toEpochMilli();
    long startOffset = sessionController.getSongStartOffset(session).plus(SessionController.SYNC_DELAY, ChronoUnit.MILLIS).toMillis();
    Optional<User> connectedUser = this.userRepository.findById(userId);
    if (connectedUser.isPresent()) {
      sessionController.addUserToSession(connectedUser.get(), session);
      return new JoinCommand(userId, minSong, getMediaFromQueue(session), getMediaFromHistory(session),
          session.getSessionState(), session.isLoopMode(), startTime, startOffset,session.getUsers());
    }
    return new NopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/leave/{userId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command leave(@DestinationVariable long sessionId, @DestinationVariable long userId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    Optional<User> disconnectedUser = session.getUsers().stream().filter(user -> user.getId() == userId).findFirst();
    if (disconnectedUser.isPresent()) {
      sessionController.removeUserFromSession(disconnectedUser.get(), session);
      return new LeaveCommand(userId);
    }
    return new NopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/shuffle")
  @SendTo("/topic/sessions/{sessionId}")
  public Command shuffle(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.shuffleQueue(session);
    return new UpdateQueueCommand(getMediaFromQueue(session));
  }

  @MessageMapping("/sessions/{sessionId}/commands/movedSong/{previousIndex}/to/{currentIndex}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command swapElements(@DestinationVariable long sessionId, @DestinationVariable int previousIndex, @DestinationVariable int currentIndex, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);

    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    Queue queue = session.getQueue();

    if (queue.getHistoryMedia().size() > previousIndex) {
      // Dragged Element is from history
      if (queue.getHistoryMedia().size() > currentIndex) {
        // Dragged into history
        Collections.swap(queue.getHistoryMedia(), previousIndex, currentIndex);
      } else {
        // dragged into queue
        Media media = queue.getHistoryMedia().remove(previousIndex);
        int queuePos = Math.max(currentIndex - (queue.getHistoryMedia().size() + 1), 0);
        queue.getQueuedMedia().add(queuePos, media);
      }
    } else if (queue.getHistoryMedia().size() < previousIndex) {
      // Dragged element is from queue
      if (queue.getHistoryMedia().size() >= currentIndex) {
        // Dragged into history
        int queuePos = Math.max(previousIndex - (queue.getHistoryMedia().size() + 1), 0);
        Media media = queue.getQueuedMedia().get(queuePos);
        queue.getHistoryMedia().add(currentIndex, media);
        queue.getQueuedMedia().remove(queuePos);
      } else {
        // Dragged into queue
        Collections.swap(queue.getQueuedMedia(), previousIndex - (queue.getHistoryMedia().size() + 1), currentIndex - (queue.getHistoryMedia().size() + 1));
      }
    }

    return new UpdateQueueAndHistoryCommand(getMediaFromQueue(session), getMediaFromHistory(session));
  }

  @MessageMapping("/sessions/{sessionId}/commands/loop/{loopMode}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command loop(@DestinationVariable long sessionId, @DestinationVariable boolean loopMode, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    session.setLoopMode(loopMode);
    return new UpdateLoopModeCommand(session.isLoopMode());
  }

  @MessageMapping("/sessions/{sessionId}/commands/end/{songId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command end(@DestinationVariable long sessionId, @DestinationVariable long songId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (this) {
      if (session.getQueue().getCurrentSong().getId() == songId) {
        sessionController.nextSong(session);
        return startSong(session);
      }
    }
    return new NopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/deleteSongFromQueue/{queueIndex}/{type}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command deleteSongFromQueue(@DestinationVariable long sessionId, @DestinationVariable int queueIndex, @DestinationVariable String type, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + queueIndex + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    switch (type) {
      case "queue" -> {
        sessionController.deleteSongFromQueue(session, queueIndex);
        System.out.println("removed from queue");
        return new UpdateQueueCommand(getMediaFromQueue(session));
      }
      case "history" -> {
        sessionController.deleteSongFromHistory(session, queueIndex);
        System.out.println("removed from history");
        return new UpdateHistoryCommand(getMediaFromHistory(session));
      }
    }
    return new NopCommand();
  }

  private Command startSong(Session session) {
    Optional<Media> currentSong = sessionController.getCurrentSong(session);
    if (currentSong.isPresent()) {
      MinimalMedia minimalSong = new MinimalMedia(currentSong.get().getId(), currentSong.get().getTitle(),currentSong.get().getType());
      long startTime = sessionController.start(session).toEpochMilli();
      return new StartCommand(minimalSong, startTime);
    }
    return new StopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/addSongToQueue")
  @SendTo("/topic/sessions/{sessionId}")
  public Command addSongToPlaylist(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    ObjectMapper mapper = new ObjectMapper();
    Media media = mapper.readValue(message, Media.class);
    session.getQueue().getQueuedMedia().add(media);
    return new UpdateQueueCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/addSongsToQueue")
  @SendTo("/topic/sessions/{sessionId}")
  public Command addSongsToPlaylist(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    ObjectMapper mapper = new ObjectMapper();
    List<Media> media = mapper.readValue(message, new TypeReference<>() {
    });
    session.getQueue().getQueuedMedia().addAll(media);
    return new UpdateQueueCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/playSongNext")
  @SendTo("/topic/sessions/{sessionId}")
  public Command playSongNext(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    ObjectMapper mapper = new ObjectMapper();
    Media media = mapper.readValue(message, Media.class);
    session.getQueue().getQueuedMedia().add(0, media);
    return new UpdateQueueCommand();
  }

  private List<MinimalMedia> getMediaFromQueue(Session session) {
    return session.getQueue().getQueuedMedia().stream()
        .map(media -> new MinimalMedia(media.getId(), media.getTitle(), media.getType()))
        .collect(Collectors.toList());
  }

  private List<MinimalMedia> getMediaFromHistory(Session session) {
    return session.getQueue().getHistoryMedia().stream()
        .map(media -> new MinimalMedia(media.getId(), media.getTitle(), media.getType()))
        .collect(Collectors.toList());
  }
}
