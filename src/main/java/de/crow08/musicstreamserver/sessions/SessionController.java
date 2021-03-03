package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.queue.Queue;
import de.crow08.musicstreamserver.song.Song;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Controller
public class SessionController {



  public Optional<Song> getCurrentSong(Session session) {
    if (session.getQueue().getCurrentSong() == null) {
      this.nextSong(session);
    }
    return Optional.ofNullable(session.getQueue().getCurrentSong());

  }

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

  public void nextSong(Session session) {
    Queue queue = session.getQueue();
    Song currentSong = queue.getCurrentSong();
    if (currentSong != null) {
      queue.getHistorySongs().add(currentSong);
    }
    if (queue.getQueuedSongs().size() > 0) {
      queue.setCurrentSong(queue.getQueuedSongs().get(0));
      queue.getQueuedSongs().remove(0);
    } else {
      queue.setCurrentSong(null);
    }
    session.setSavedProgression(Duration.ZERO);
  }

  public void addSongs(Session session, List<Song> songs) {
    session.getQueue().getQueuedSongs().addAll(songs);
  }

  public void start(Session session) {
    session.setSongStarted(Instant.now().plus(1, ChronoUnit.SECONDS));
    session.setSavedProgression(Duration.ZERO);
    session.setSessionState(Session.SessionState.PLAY);
  }

  public void pause(Session session) {
    Instant now = Instant.now();
    if(session.getSongStarted() != null) {
      session.setSavedProgression(session.getSavedProgression().plus(Duration.between(session.getSongStarted(), now)));
    }
    session.setSongStarted(null);
    session.setSessionState(Session.SessionState.PAUSE);
  }

  public void resume(Session session) {
    Instant now = Instant.now();
    session.setSongStarted(now.plus(1, ChronoUnit.SECONDS));
    session.setSessionState(Session.SessionState.PLAY);
  }

  public void stop(Session session) {
    session.setSavedProgression(Duration.ZERO);
    session.setSongStarted(null);
    session.setSessionState(Session.SessionState.STOP);
  }

  public void skip(Session session) {
    this.start(session);
  }
}
