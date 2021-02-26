package de.crow08.musicstreamserver.wscommunication;

import de.crow08.musicstreamserver.sessions.MusicSession;
import de.crow08.musicstreamserver.sessions.SessionRepository;
import de.crow08.musicstreamserver.wscommunication.commands.Command;
import de.crow08.musicstreamserver.wscommunication.commands.PauseCommand;
import de.crow08.musicstreamserver.wscommunication.commands.ResumeCommand;
import de.crow08.musicstreamserver.wscommunication.commands.SkipCommand;
import de.crow08.musicstreamserver.wscommunication.commands.StartCommand;
import de.crow08.musicstreamserver.wscommunication.commands.StopCommand;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Controller
public class PlayerControlsController {

  final  SessionRepository sessionRepository;

  public PlayerControlsController(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  @MessageMapping("/sessions/{sessionId}/commands/start")
  @SendTo("/topic/sessions/{sessionId}")
  public Command start(@DestinationVariable String sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if(sessionRepository.findById(sessionId).isPresent()) {
      MusicSession session = sessionRepository.findById(sessionId).get();
      long startTime = Instant.now().plus(1, ChronoUnit.SECONDS).toEpochMilli();
      if(session.getCurrentSong().isPresent()){
        String songId = session.getCurrentSong().get().getId().toString();
        return new StartCommand(songId, startTime);
      }
      throw new Exception("No current song");

    }
    throw new Exception("Session not found");
  }

  @MessageMapping("/sessions/{sessionId}/commands/pause")
  @SendTo("/topic/sessions/{sessionId}")
  public Command pause(@DestinationVariable String sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if(sessionRepository.findById(sessionId).isPresent()){
      MusicSession session = sessionRepository.findById(sessionId).get();
      if(session.getcurrentSongStartedTime().isPresent()) {
        long pausePosition = Duration.between(Instant.now(), session.getcurrentSongStartedTime().get()).get(ChronoUnit.MILLIS);
        return new PauseCommand(pausePosition);
      }
      throw new Exception("No current song");
    }
    throw new Exception("Session not found");
  }

  @MessageMapping("/sessions/{sessionId}/commands/resume")
  @SendTo("/topic/sessions/{sessionId}")
  public Command resume(@DestinationVariable String sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if(sessionRepository.findById(sessionId).isPresent()){
      long startTime = Instant.now().plus(2, ChronoUnit.SECONDS).toEpochMilli();
      return new ResumeCommand(startTime);
    }
    throw new Exception("Session not found");
  }

  @MessageMapping("/sessions/{sessionId}/commands/stop")
  @SendTo("/topic/sessions/{sessionId}")
  public Command stop(@DestinationVariable String sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if(sessionRepository.findById(sessionId).isPresent()){
      return new StopCommand();
    }
    throw new Exception("Session not found");
  }

  @MessageMapping("/sessions/{sessionId}/commands/skip")
  @SendTo("/topic/sessions/{sessionId}")
  public Command skip(@DestinationVariable String sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if(sessionRepository.findById(sessionId).isPresent()) {
      MusicSession session = sessionRepository.findById(sessionId).get();
      session.nextSong();
      long startTime = Instant.now().plus(1, ChronoUnit.SECONDS).toEpochMilli();
      if(session.getCurrentSong().isPresent()){
        String songId = session.getCurrentSong().get().getId().toString();
        return new SkipCommand(songId, startTime);
      }
      throw new Exception("No current song");
    }
    throw new Exception("Session not found");
  }
}
