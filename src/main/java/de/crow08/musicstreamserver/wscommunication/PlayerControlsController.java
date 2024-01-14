package de.crow08.musicstreamserver.wscommunication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.crow08.musicstreamserver.model.media.Media;
import de.crow08.musicstreamserver.model.media.MinimalMedia;
import de.crow08.musicstreamserver.model.queue.Queue;
import de.crow08.musicstreamserver.model.users.User;
import de.crow08.musicstreamserver.model.users.UserRepository;
import de.crow08.musicstreamserver.sessions.Session;
import de.crow08.musicstreamserver.sessions.SessionController;
import de.crow08.musicstreamserver.sessions.SessionRepository;
import de.crow08.musicstreamserver.wscommunication.commands.Command;
import de.crow08.musicstreamserver.wscommunication.commands.JoinCommand;
import de.crow08.musicstreamserver.wscommunication.commands.JumpCommand;
import de.crow08.musicstreamserver.wscommunication.commands.LeaveCommand;
import de.crow08.musicstreamserver.wscommunication.commands.NopCommand;
import de.crow08.musicstreamserver.wscommunication.commands.PauseCommand;
import de.crow08.musicstreamserver.wscommunication.commands.ResumeCommand;
import de.crow08.musicstreamserver.wscommunication.commands.SetMediaCommand;
import de.crow08.musicstreamserver.wscommunication.commands.StartCommand;
import de.crow08.musicstreamserver.wscommunication.commands.StopCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateHistoryCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateLoopModeCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateQueueAndHistoryCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateQueueCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PlayerControlsController {

  private static final long JOIN_DELAY = 3000L;
  private final SessionRepository sessionRepository;
  private final SessionController sessionController;
  private final UserRepository userRepository;

  private SimpMessageSendingOperations messagingTemplate;

  @Autowired
  public PlayerControlsController(SessionRepository sessionRepository,
                                  SessionController sessionController,
                                  UserRepository userRepository,
                                  SimpMessageSendingOperations messagingTemplate) {
    this.sessionRepository = sessionRepository;
    this.sessionController = sessionController;
    this.userRepository = userRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping("/sessions/{sessionId}/commands/start")
  @SendTo("/topic/sessions/{sessionId}")
  public Command start(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.start(session);
    return setMedia(session);
  }

  @MessageMapping("/sessions/{sessionId}/commands/jump/{offset}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command jump(@DestinationVariable long sessionId, @DestinationVariable long offset, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    Duration startOffsetDuration = sessionController.moveMediaStartOffset(session, offset);
    sessionController.enterSyncState(session);
    return new JumpCommand(startOffsetDuration.toMillis());
  }

  @MessageMapping("/sessions/{sessionId}/commands/skip")
  @SendTo("/topic/sessions/{sessionId}")
  public Command skip(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.nextMedia(session);
    return setMedia(session);
  }

  @MessageMapping("/sessions/{sessionId}/commands/previous")
  @SendTo("/topic/sessions/{sessionId}")
  public Command previous(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.previousMedia(session);
    return setMedia(session);
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
    Instant startTime = sessionController.resume(session);
    if (startTime != null) {
      return new ResumeCommand(startTime.toEpochMilli());
    } else {
      return new NopCommand();
    }
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
    Optional<Media> currentMedia = sessionController.getCurrentMedia(session);
    MinimalMedia minMedia = null;
    if (currentMedia.isPresent()) {
      minMedia = new MinimalMedia(currentMedia.get().getId(), currentMedia.get().getTitle(), currentMedia.get().getType());
    }
    long startTime = Instant.now().plus(JOIN_DELAY, ChronoUnit.MILLIS).toEpochMilli();
    long startOffset = session.getSessionState().equals(Session.SessionState.PLAY) ?
        sessionController.getMediaStartOffset(session).plus(JOIN_DELAY, ChronoUnit.MILLIS).toMillis() :
        sessionController.getMediaStartOffset(session).toMillis();
    Optional<User> connectedUser = this.userRepository.findById(userId);
    if (connectedUser.isPresent()) {
      sessionController.addUserToSession(connectedUser.get(), session);
      System.out.println("start offset:" + startOffset);
      return new JoinCommand(userId, minMedia, getMediaFromQueue(session), getMediaFromHistory(session),
          session.getSessionState(), session.isLoopMode(), startTime, startOffset, session.getUsers());
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

  @MessageMapping("/sessions/{sessionId}/commands/movedMedia/{previousIndex}/to/{currentIndex}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command movedMedia(@DestinationVariable long sessionId, @DestinationVariable int previousIndex, @DestinationVariable int currentIndex, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);

    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getQueue()) {
      Queue queue = session.getQueue();

      if (queue.getHistoryMedia().size() > previousIndex) {
        // Dragged Element is from history
        if (queue.getHistoryMedia().size() > currentIndex) {
          // Dragged into history
          Media media = queue.getHistoryMedia().remove(previousIndex);
          queue.getHistoryMedia().add(currentIndex, media);
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
          Media media = queue.getQueuedMedia().remove(previousIndex - (queue.getHistoryMedia().size() + 1));
          queue.getQueuedMedia().add(currentIndex - (queue.getHistoryMedia().size() + 1), media);
        }
      }
    }
    return new UpdateQueueAndHistoryCommand(getMediaFromQueue(session), getMediaFromHistory(session));
  }

  @MessageMapping("/sessions/{sessionId}/commands/skipTo/{mediaId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command skipTo(@Payload Message msg, @DestinationVariable long sessionId, @DestinationVariable int mediaId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);

    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getQueue()) {
      Queue queue = session.getQueue();
      Media currentMedia;
      if (queue.getQueuedMedia().stream().anyMatch(media -> media.getId() == mediaId)) {
        do {
          sessionController.nextMedia(session);
          currentMedia = queue.getCurrentMedia();
        } while (currentMedia != null && currentMedia.getId() != mediaId);
      } else if (queue.getHistoryMedia().stream().anyMatch(media -> media.getId() == mediaId)) {
        do {
          sessionController.previousMedia(session);
          currentMedia = queue.getCurrentMedia();
        } while (currentMedia != null && currentMedia.getId() != mediaId);
      }
    }
    UpdateQueueAndHistoryCommand updateQueueAndHistoryCommand = new UpdateQueueAndHistoryCommand(getMediaFromQueue(session), getMediaFromHistory(session));
    this.messagingTemplate.convertAndSend("/topic/sessions/" + sessionId, updateQueueAndHistoryCommand, createHeaders(msg));
    return setMedia(session);
  }

  private MessageHeaders createHeaders(Message msg) {
    MessageHeaders headers = msg.getHeaders();
    String msgSessionId = SimpMessageHeaderAccessor.getSessionId(headers);
    SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
    if (msgSessionId != null) {
      headerAccessor.setSessionId(msgSessionId);
    }
    headerAccessor.setLeaveMutable(true);
    return headerAccessor.getMessageHeaders();
  }


  @MessageMapping("/sessions/{sessionId}/commands/loop/{loopMode}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command loop(@DestinationVariable long sessionId, @DestinationVariable boolean loopMode, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    session.setLoopMode(loopMode);
    return new UpdateLoopModeCommand(session.isLoopMode());
  }

  @MessageMapping("/sessions/{sessionId}/commands/end/{mediaId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command end(@DestinationVariable long sessionId, @DestinationVariable long mediaId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getQueue()) {
      if (session.getQueue().getCurrentMedia().getId() == mediaId) {
        sessionController.nextMedia(session);
        return setMedia(session);
      }
    }
    return new NopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/deleteMediaFromQueue/{queueIndex}/{type}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command deleteMediaFromQueue(@DestinationVariable long sessionId, @DestinationVariable int queueIndex, @DestinationVariable String type, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + queueIndex + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    switch (type) {
      case "queue" -> {
        sessionController.deleteMediaFromQueue(session, queueIndex);
        System.out.println("removed from queue");
        return new UpdateQueueCommand(getMediaFromQueue(session));
      }
      case "history" -> {
        sessionController.deleteMediaFromHistory(session, queueIndex);
        System.out.println("removed from history");
        return new UpdateHistoryCommand(getMediaFromHistory(session));
      }
    }
    return new NopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/ready/{userId}/{mediaId}/{duration}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command ready(@DestinationVariable long sessionId, @DestinationVariable long userId, @DestinationVariable long mediaId, @DestinationVariable long duration, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Optional<User> connectedUser = this.userRepository.findById(userId);
    Optional<Session> session = sessionRepository.findById(sessionId);
    if (connectedUser.isPresent() && session.isPresent()) {
      boolean allUsersReady = sessionController.userIsReady(connectedUser.get(), session.get(), mediaId, duration);
      System.out.println("User " + userId + " Ready");
      System.out.println("ALL?" + allUsersReady);
      if (allUsersReady) {
        if (session.get().getSessionState().equals(Session.SessionState.SYNC_PLAY)) {
          sessionController.leaveSyncState(session.get());
          Instant startTime = sessionController.resume(session.get());
          if (startTime != null) {
            return new StartCommand(startTime.toEpochMilli());
          } else {
            throw new RuntimeException("Something went wrong. Sync has not finished properly.");
          }
        }
        sessionController.leaveSyncState(session.get());
      }
    }
    return new NopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/addMediaToQueue")
  @SendTo("/topic/sessions/{sessionId}")
  public Command addMediaToPlaylist(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    ObjectMapper mapper = new ObjectMapper();
    Media media = mapper.readValue(message, Media.class);
    synchronized (session.getQueue()) {
      session.getQueue().getQueuedMedia().add(media);
    }
    return new UpdateQueueCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/addMediaListToQueue")
  @SendTo("/topic/sessions/{sessionId}")
  public Command addMediaListToPlaylist(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    ObjectMapper mapper = new ObjectMapper();
    List<Media> media = mapper.readValue(message, new TypeReference<>() {
    });
    synchronized (session.getQueue()) {
      session.getQueue().getQueuedMedia().addAll(media);
    }
    return new UpdateQueueCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/playMediaNext")
  @SendTo("/topic/sessions/{sessionId}")
  public Command playMediaNext(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    ObjectMapper mapper = new ObjectMapper();
    Media media = mapper.readValue(message, Media.class);
    synchronized (session.getQueue()) {
      session.getQueue().getQueuedMedia().add(0, media);
    }
    return new UpdateQueueCommand();
  }

  private Command setMedia(Session session) {
    Optional<Media> currentMedia = sessionController.getCurrentMedia(session);
    if (currentMedia.isPresent()) {
      MinimalMedia minimalMedia = new MinimalMedia(currentMedia.get().getId(), currentMedia.get().getTitle(), currentMedia.get().getType());
      sessionController.enterSyncState(session);
      return new SetMediaCommand(minimalMedia);
    }
    return new StopCommand();
  }

  private List<MinimalMedia> getMediaFromQueue(Session session) {
    synchronized (session.getQueue()) {
      return session.getQueue().getQueuedMedia().stream()
          .map(media -> new MinimalMedia(media.getId(), media.getTitle(), media.getType()))
          .collect(Collectors.toList());
    }
  }

  private List<MinimalMedia> getMediaFromHistory(Session session) {
    synchronized (session.getQueue()) {
      return session.getQueue().getHistoryMedia().stream()
          .map(media -> new MinimalMedia(media.getId(), media.getTitle(), media.getType()))
          .collect(Collectors.toList());
    }
  }
}
