package de.crow08.musicstreamserver.wscommunication;

import de.crow08.musicstreamserver.sessions.Session;
import de.crow08.musicstreamserver.sessions.SessionController;
import de.crow08.musicstreamserver.sessions.SessionRepository;
import de.crow08.musicstreamserver.song.Song;
import de.crow08.musicstreamserver.wscommunication.commands.Command;
import de.crow08.musicstreamserver.wscommunication.commands.JoinCommand;
import de.crow08.musicstreamserver.wscommunication.commands.PauseCommand;
import de.crow08.musicstreamserver.wscommunication.commands.ResumeCommand;
import de.crow08.musicstreamserver.wscommunication.commands.SkipCommand;
import de.crow08.musicstreamserver.wscommunication.commands.StartCommand;
import de.crow08.musicstreamserver.wscommunication.commands.StopCommand;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
public class PlayerControlsController {

  final SessionRepository sessionRepository;
  final SessionController sessionController;

  public PlayerControlsController(SessionRepository sessionRepository, SessionController sessionController) {
    this.sessionRepository = sessionRepository;
    this.sessionController = sessionController;
  }

  @MessageMapping("/sessions/{sessionId}/commands/start")
  @SendTo("/topic/sessions/{sessionId}")
  public Command start(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    Optional<Song> currentSong = sessionController.getCurrentSong(session);
    if (currentSong.isPresent()) {
      long songId = currentSong.get().getId();
      long startTime = sessionController.start(session).toEpochMilli();
      return new StartCommand(songId, startTime);
    }
    return new StopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/pause")
  @SendTo("/topic/sessions/{sessionId}")
  public Command pause(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    long pausePosition = sessionController.pause(session).toMillis();
    return new PauseCommand(pausePosition);
  }

  @MessageMapping("/sessions/{sessionId}/commands/resume")
  @SendTo("/topic/sessions/{sessionId}")
  public Command resume(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    long startTime = sessionController.resume(session).toEpochMilli();
    return new ResumeCommand(startTime);
  }

  @MessageMapping("/sessions/{sessionId}/commands/stop")
  @SendTo("/topic/sessions/{sessionId}")
  public Command stop(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.stop(session);
    return new StopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/skip")
  @SendTo("/topic/sessions/{sessionId}")
  public Command skip(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    sessionController.nextSong(session);
    Optional<Song> currentSong = sessionController.getCurrentSong(session);
    if (currentSong.isPresent()) {
      long songId = currentSong.get().getId();
      long startTime = sessionController.skip(session).toEpochMilli();
      return new SkipCommand(songId, startTime);
    }
    return new StopCommand();
  }

  @MessageMapping("/sessions/{sessionId}/commands/join/{userId}")
  @SendTo("/topic/sessions/{sessionId}")
  public Command join(@DestinationVariable long sessionId, @DestinationVariable long userId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    Optional<Song> currentSong = sessionController.getCurrentSong(session);
    long songId = -1;
    if (currentSong.isPresent()) {
      songId = currentSong.get().getId();
    }
    long startTime = Instant.now().plus(SessionController.SYNC_DELAY, ChronoUnit.MILLIS).toEpochMilli();
    long startOffset = sessionController.getSongStartOffset(session).plus(SessionController.SYNC_DELAY, ChronoUnit.MILLIS).toMillis();
    return new JoinCommand(songId, startTime, startOffset, session.getSessionState(), userId);
  }
}
