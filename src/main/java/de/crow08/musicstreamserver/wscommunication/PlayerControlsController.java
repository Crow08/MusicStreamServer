package de.crow08.musicstreamserver.wscommunication;

import de.crow08.musicstreamserver.sessions.MusicSession;
import de.crow08.musicstreamserver.sessions.SessionRepository;
import de.crow08.musicstreamserver.song.Song;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
public class PlayerControlsController {

  final SessionRepository sessionRepository;

  public PlayerControlsController(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  @MessageMapping("/sessions/{sessionId}/commands/start")
  @SendTo("/topic/sessions/{sessionId}")
  public Command start(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if (sessionRepository.findById(sessionId).isPresent()) {
      MusicSession session = sessionRepository.findById(sessionId).get();
      Instant startTime = Instant.now().plus(1, ChronoUnit.SECONDS);
      Optional<Song> currentSong = session.getCurrentSong();
      if (currentSong.isPresent()) {
        long songId = currentSong.get().getId();
        session.setState(MusicSession.SessionState.PLAY);
        return new StartCommand(songId, startTime.toEpochMilli());
      }
      return new StopCommand();

    }
    throw new Exception("Session not found");
  }

  @MessageMapping("/sessions/{sessionId}/commands/pause")
  @SendTo("/topic/sessions/{sessionId}")
  public Command pause(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if (sessionRepository.findById(sessionId).isPresent()) {
      MusicSession session = sessionRepository.findById(sessionId).get();
      long pausePosition = session.getSongStartOffset().toMillis();
      session.setState(MusicSession.SessionState.PAUSE);
      return new PauseCommand(pausePosition);
    }
    throw new Exception("Session not found");
  }

  @MessageMapping("/sessions/{sessionId}/commands/resume")
  @SendTo("/topic/sessions/{sessionId}")
  public Command resume(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if (sessionRepository.findById(sessionId).isPresent()) {
      MusicSession session = sessionRepository.findById(sessionId).get();
      Instant startTime = Instant.now().plus(1, ChronoUnit.SECONDS);
      session.setState(MusicSession.SessionState.PLAY);
      return new ResumeCommand(startTime.toEpochMilli());
    }
    throw new Exception("Session not found");
  }

  @MessageMapping("/sessions/{sessionId}/commands/stop")
  @SendTo("/topic/sessions/{sessionId}")
  public Command stop(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if (sessionRepository.findById(sessionId).isPresent()) {
      MusicSession session = sessionRepository.findById(sessionId).get();
      session.setState(MusicSession.SessionState.STOP);
      return new StopCommand();
    }
    throw new Exception("Session not found");
  }

  @MessageMapping("/sessions/{sessionId}/commands/skip")
  @SendTo("/topic/sessions/{sessionId}")
  public Command skip(@DestinationVariable long sessionId, String message) throws Exception {
    System.out.println("Recieved: " + sessionId + " - " + message);
    if (sessionRepository.findById(sessionId).isPresent()) {
      MusicSession session = sessionRepository.findById(sessionId).get();
      session.nextSong();
      long startTime = Instant.now().plus(1, ChronoUnit.SECONDS).toEpochMilli();
      Optional<Song> currentSong = session.getCurrentSong();
      if (currentSong.isPresent()) {
        long songId = currentSong.get().getId();
        session.setState(MusicSession.SessionState.PLAY);
        return new SkipCommand(songId, startTime);
      }
      return new StopCommand();
    }
    throw new Exception("Session not found");
  }
}
