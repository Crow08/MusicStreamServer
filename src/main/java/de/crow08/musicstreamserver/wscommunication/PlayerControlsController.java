package de.crow08.musicstreamserver.wscommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.crow08.musicstreamserver.model.media.Media;
import de.crow08.musicstreamserver.model.media.MediaRepository;
import de.crow08.musicstreamserver.model.media.MinimalMedia;
import de.crow08.musicstreamserver.model.queue.Queue;
import de.crow08.musicstreamserver.model.users.User;
import de.crow08.musicstreamserver.model.users.UserRepository;
import de.crow08.musicstreamserver.sessions.Session;
import de.crow08.musicstreamserver.sessions.SessionController;
import de.crow08.musicstreamserver.sessions.SessionRepository;
import de.crow08.musicstreamserver.sessions.UserStatus;
import de.crow08.musicstreamserver.wscommunication.commands.Command;
import de.crow08.musicstreamserver.wscommunication.commands.ErrorCommand;
import de.crow08.musicstreamserver.wscommunication.commands.JoinCommand;
import de.crow08.musicstreamserver.wscommunication.commands.JumpCommand;
import de.crow08.musicstreamserver.wscommunication.commands.LeaveCommand;
import de.crow08.musicstreamserver.wscommunication.commands.NopCommand;
import de.crow08.musicstreamserver.wscommunication.commands.PauseCommand;
import de.crow08.musicstreamserver.wscommunication.commands.ResumeCommand;
import de.crow08.musicstreamserver.wscommunication.commands.SetMediaCommand;
import de.crow08.musicstreamserver.wscommunication.commands.StopCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateHistoryCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateLoopModeCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateQueueAndHistoryCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateQueueCommand;
import de.crow08.musicstreamserver.wscommunication.commands.UpdateUserStatusCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PlayerControlsController {

  private static final long JOIN_DELAY = 3000L;
  /** Minimum playback time before an end report is accepted, see {@link #end}. */
  private static final long END_GRACE_PERIOD = 2000L;
  private static final String QUEUE_CHANGED = "The queue has changed, please try again";
  private final SessionRepository sessionRepository;
  private final SessionController sessionController;
  private final UserRepository userRepository;
  private final MediaRepository mediaRepository;
  private final WebSocketSessionController webSocketSessionController;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  public PlayerControlsController(SessionRepository sessionRepository,
                                  SessionController sessionController,
                                  UserRepository userRepository,
                                  MediaRepository mediaRepository,
                                  WebSocketSessionController webSocketSessionController) {
    this.sessionRepository = sessionRepository;
    this.sessionController = sessionController;
    this.userRepository = userRepository;
    this.mediaRepository = mediaRepository;
    this.webSocketSessionController = webSocketSessionController;
  }

  @MessageMapping("/sessions/{sessionId}/commands/start")
  @SendTo("/topic/sessions/{sessionId}")
  public Command start(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      sessionController.start(session);
      return setMedia(session);
    }
  }

  @MessageMapping("/sessions/{sessionId}/commands/jump/{offset}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command jump(@DestinationVariable long sessionId, @DestinationVariable long offset, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      Duration startOffsetDuration = sessionController.moveMediaStartOffset(session, offset);
      sessionController.enterSyncState(session);
      return new JumpCommand(startOffsetDuration.toMillis());
    }
  }

  @MessageMapping("/sessions/{sessionId}/commands/skip")
  @SendTo("/topic/sessions/{sessionId}")
  public Command skip(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      sessionController.nextMedia(session);
      return setMedia(session);
    }
  }

  @MessageMapping("/sessions/{sessionId}/commands/previous")
  @SendTo("/topic/sessions/{sessionId}")
  public Command previous(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      sessionController.previousMedia(session);
      return setMedia(session);
    }
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
  public Command join(@DestinationVariable long sessionId, @DestinationVariable long userId, Principal principal,
                      @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String wsSessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      // read without side effects: sessionController.getCurrentMedia() would move the first queued media into place
      Media currentMedia = session.getQueue().getCurrentMedia();
      MinimalMedia minMedia = null;
      if (currentMedia != null) {
        minMedia = new MinimalMedia(currentMedia.getId(), currentMedia.getTitle(), currentMedia.getType());
      }
      long startTime = Instant.now().plus(JOIN_DELAY, ChronoUnit.MILLIS).toEpochMilli();
      long startOffset = session.getSessionState().equals(Session.SessionState.PLAY) ?
          sessionController.getMediaStartOffset(session).plus(JOIN_DELAY, ChronoUnit.MILLIS).toMillis() :
          sessionController.getMediaStartOffset(session).toMillis();
      Optional<User> connectedUser = getConnectedUser(principal, userId);
      if (connectedUser.isPresent()) {
        webSocketSessionController.joinMusicSession(wsSessionId, sessionId);
        sessionController.addUserToSession(connectedUser.get(), session);
        System.out.println("start offset:" + startOffset);
        return new JoinCommand(userId, minMedia, getMediaFromQueue(session), getMediaFromHistory(session),
            session.getSessionState(), session.isLoopMode(), startTime, startOffset, session.getUsers(),
            new ArrayList<>(session.getUserStatuses()));
      }
    }
    return new NopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/leave/{userId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command leave(@DestinationVariable long sessionId, @DestinationVariable long userId, Principal principal,
                       @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String wsSessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    if (getConnectedUser(principal, userId).isEmpty()) {
      return new NopCommand();
    }
    // the user stays in the session as long as another connection (e.g. browser tab) of it is still in there
    if (!webSocketSessionController.leaveMusicSession(wsSessionId, userId, sessionId)) {
      return new NopCommand();
    }
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

  /**
   * Moves media within the combined list of history, current media and queue (as displayed by the client).
   * The indices refer to the client's view, which may be outdated: the moved media is identified by its id.
   */
  @MessageMapping("/sessions/{sessionId}/commands/movedMedia/{requestedPreviousIndex}/to/{currentIndex}/{mediaId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command movedMedia(@DestinationVariable long sessionId, @DestinationVariable int requestedPreviousIndex,
                            @DestinationVariable int currentIndex, @DestinationVariable long mediaId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);

    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      Queue queue = session.getQueue();
      int previousIndex = findInHistoryAndQueue(queue, requestedPreviousIndex, mediaId);
      if (previousIndex < 0) {
        throw new Exception(QUEUE_CHANGED);
      }

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

  /**
   * Jumps to a media in the history or queue. The index refers to the client's view, which may be outdated: the media
   * is identified by its id, the index selects one of several occurrences.
   */
  @MessageMapping("/sessions/{sessionId}/commands/skipTo/{requestedIndex}/{mediaId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command skipTo(@DestinationVariable long sessionId, @DestinationVariable int requestedIndex,
                        @DestinationVariable long mediaId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);

    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      int index = findInHistoryAndQueue(session.getQueue(), requestedIndex, mediaId);
      if (index < 0) {
        throw new Exception(QUEUE_CHANGED);
      }
      sessionController.skipTo(session, index);
      return setMedia(session);
    }
  }


  @MessageMapping("/sessions/{sessionId}/commands/loop/{loopMode}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command loop(@DestinationVariable long sessionId, @DestinationVariable boolean loopMode, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      session.setLoopMode(loopMode);
      return new UpdateLoopModeCommand(session.isLoopMode());
    }
  }

  @MessageMapping("/sessions/{sessionId}/commands/end/{mediaId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command end(@DestinationVariable long sessionId, @DestinationVariable long mediaId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      Media currentMedia = session.getQueue().getCurrentMedia();
      // every client reports the end: only the first report for a playback that actually ran is accepted, so a slow
      // client can not skip the next media, even if it is the same media again (e.g. loop mode with one media)
      boolean playedLongEnough = session.getSessionState().equals(Session.SessionState.PLAY) &&
          sessionController.getMediaStartOffset(session).toMillis() >= END_GRACE_PERIOD;
      if (currentMedia != null && currentMedia.getId() == mediaId && playedLongEnough) {
        sessionController.nextMedia(session);
        return setMedia(session);
      }
    }
    return new NopCommand();
  }

  /**
   * Deletes media from the queue or history. The index refers to the client's view, which may be outdated: the media
   * is identified by its id.
   */
  @MessageMapping("/sessions/{sessionId}/commands/deleteMediaFromQueue/{queueIndex}/{type}/{mediaId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command deleteMediaFromQueue(@DestinationVariable long sessionId, @DestinationVariable int queueIndex,
                                      @DestinationVariable String type, @DestinationVariable long mediaId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + queueIndex + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    synchronized (session.getLock()) {
      switch (type) {
        case "queue" -> {
          int index = findNearest(session.getQueue().getQueuedMedia(), queueIndex, mediaId);
          if (index < 0) {
            throw new Exception(QUEUE_CHANGED);
          }
          sessionController.deleteMediaFromQueue(session, index);
          return new UpdateQueueCommand(getMediaFromQueue(session));
        }
        case "history" -> {
          int index = findNearest(session.getQueue().getHistoryMedia(), queueIndex, mediaId);
          if (index < 0) {
            throw new Exception(QUEUE_CHANGED);
          }
          sessionController.deleteMediaFromHistory(session, index);
          return new UpdateHistoryCommand(getMediaFromHistory(session));
        }
        default -> throw new Exception("Unknown list: " + type);
      }
    }
  }

  /**
   * Finds the media with the given id closest to the expected index (the same media can be queued multiple times).
   *
   * @return the index or -1 if the media is not in the list.
   */
  private static int findNearest(List<Media> list, int expectedIndex, long mediaId) {
    int found = -1;
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).getId() == mediaId && (found < 0 || Math.abs(i - expectedIndex) < Math.abs(found - expectedIndex))) {
        found = i;
      }
    }
    return found;
  }

  /**
   * Like {@link #findNearest} for the combined list of history, current media (not movable) and queue.
   */
  private static int findInHistoryAndQueue(Queue queue, int expectedIndex, long mediaId) {
    int historySize = queue.getHistoryMedia().size();
    int inHistory = findNearest(queue.getHistoryMedia(), expectedIndex, mediaId);
    int inQueue = findNearest(queue.getQueuedMedia(), expectedIndex - historySize - 1, mediaId);
    int queueIndex = inQueue < 0 ? -1 : inQueue + historySize + 1;
    if (inHistory < 0 || queueIndex < 0) {
      return Math.max(inHistory, queueIndex);
    }
    return Math.abs(inHistory - expectedIndex) <= Math.abs(queueIndex - expectedIndex) ? inHistory : queueIndex;
  }

  @MessageMapping("/sessions/{sessionId}/commands/ready/{userId}/{mediaId}/{duration}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command ready(@DestinationVariable long sessionId, @DestinationVariable long userId, @DestinationVariable long mediaId, @DestinationVariable long duration, Principal principal, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Optional<User> connectedUser = getConnectedUser(principal, userId);
    Optional<Session> session = sessionRepository.findById(sessionId);
    if (connectedUser.isPresent() && session.isPresent()) {
      // duration is not compared: browsers snap the media position differently, the server offset is authoritative
      return sessionController.userIsReady(connectedUser.get(), session.get(), mediaId).orElseGet(NopCommand::new);
    }
    return new NopCommand();
  }

  /**
   * Stores the playback status reported by a client (loading/ready, latency, drift) and shows it to the session.
   * Reported regularly while playing, so the message is not logged.
   */
  @MessageMapping("/sessions/{sessionId}/commands/status/{userId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command status(@DestinationVariable long sessionId, @DestinationVariable long userId, Principal principal,
                        String message) throws Exception {
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    if (getConnectedUser(principal, userId).isEmpty()) {
      return new NopCommand();
    }
    UserStatus status = UserStatus.fromReport(userId, objectMapper.readValue(message, UserStatus.class));
    synchronized (session.getLock()) {
      // a late report must not bring back the status of a user that already left the session
      if (session.getUsers().stream().noneMatch(user -> user.getId() == userId)) {
        return new NopCommand();
      }
      session.setUserStatus(status);
    }
    return new UpdateUserStatusCommand(status);
  }

  @MessageMapping("/sessions/{sessionId}/commands/addMediaToQueue")
  @SendTo("/topic/sessions/{sessionId}")
  public Command addMediaToPlaylist(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    Media media = findMedia(objectMapper.readTree(message)).orElseThrow(() -> new Exception("Media not found"));
    synchronized (session.getLock()) {
      session.getQueue().getQueuedMedia().add(media);
    }
    return new UpdateQueueCommand(getMediaFromQueue(session));
  }

  @MessageMapping("/sessions/{sessionId}/commands/addMediaListToQueue")
  @SendTo("/topic/sessions/{sessionId}")
  public Command addMediaListToPlaylist(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    List<Media> media = new ArrayList<>();
    for (JsonNode node : objectMapper.readTree(message)) {
      findMedia(node).ifPresent(media::add);
    }
    if (media.isEmpty()) {
      throw new Exception("Media not found");
    }
    synchronized (session.getLock()) {
      session.getQueue().getQueuedMedia().addAll(media);
    }
    return new UpdateQueueCommand(getMediaFromQueue(session));
  }

  @MessageMapping("/sessions/{sessionId}/commands/playMediaNext")
  @SendTo("/topic/sessions/{sessionId}")
  public Command playMediaNext(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    Media media = findMedia(objectMapper.readTree(message)).orElseThrow(() -> new Exception("Media not found"));
    synchronized (session.getLock()) {
      session.getQueue().getQueuedMedia().add(0, media);
    }
    return new UpdateQueueCommand(getMediaFromQueue(session));
  }

  /**
   * Resolves the user of the WebSocket connection. The user id in the destination is only accepted if it matches, so a
   * client can not act on behalf of another user.
   */
  private Optional<User> getConnectedUser(Principal principal, long userId) {
    User user = principal != null ? userRepository.findByUsername(principal.getName()) : null;
    if (user == null || user.getId() != userId) {
      System.out.println("Rejected command for user " + userId + " from " + (principal != null ? principal.getName() : null));
      return Optional.empty();
    }
    return Optional.of(user);
  }

  /**
   * Resolves media sent by a client via its id. The remaining client data is not trusted.
   */
  private Optional<Media> findMedia(JsonNode mediaNode) {
    JsonNode id = mediaNode.get("id");
    if (id == null || !id.canConvertToLong()) {
      return Optional.empty();
    }
    return mediaRepository.findById(id.asLong());
  }

  @MessageMapping("/sessions/{sessionId}/commands/playMediaNow")
  @SendTo("/topic/sessions/{sessionId}")
  public Command playMediaNow(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Received: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    Media media = findMedia(objectMapper.readTree(message)).orElseThrow(() -> new Exception("Media not found"));
    synchronized (session.getLock()) {
      session.getQueue().getQueuedMedia().add(0, media);
      sessionController.nextMedia(session);
      return setMedia(session);
    }
  }

  /**
   * Reports a failed command only to the connection that sent it, instead of failing silently.
   */
  @MessageExceptionHandler
  @SendToUser(destinations = "/queue/errors", broadcast = false)
  public Command handleException(Exception exception,
                                 @Header(SimpMessageHeaderAccessor.DESTINATION_HEADER) String destination) {
    String reason = exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName();
    System.out.println("Command failed: " + destination + " - " + reason);
    return new ErrorCommand(reason, destination);
  }

  /**
   * Announces the current media. The queue and history are sent along: clients can not derive them from the media,
   * which may occur several times in the queue and history.
   */
  private Command setMedia(Session session) {
    synchronized (session.getLock()) {
      Optional<Media> currentMedia = sessionController.getCurrentMedia(session);
      if (currentMedia.isPresent()) {
        MinimalMedia minimalMedia = new MinimalMedia(currentMedia.get().getId(), currentMedia.get().getTitle(), currentMedia.get().getType());
        sessionController.enterSyncState(session);
        return new SetMediaCommand(minimalMedia, getMediaFromQueue(session), getMediaFromHistory(session));
      }
      return new StopCommand();
    }
  }

  private List<MinimalMedia> getMediaFromQueue(Session session) {
    synchronized (session.getLock()) {
      return session.getQueue().getQueuedMedia().stream()
          .map(media -> new MinimalMedia(media.getId(), media.getTitle(), media.getType()))
          .collect(Collectors.toList());
    }
  }

  private List<MinimalMedia> getMediaFromHistory(Session session) {
    synchronized (session.getLock()) {
      return session.getQueue().getHistoryMedia().stream()
          .map(media -> new MinimalMedia(media.getId(), media.getTitle(), media.getType()))
          .collect(Collectors.toList());
    }
  }
}
