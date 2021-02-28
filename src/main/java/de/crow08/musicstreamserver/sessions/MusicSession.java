package de.crow08.musicstreamserver.sessions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.queue.Queue;
import de.crow08.musicstreamserver.song.Song;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class MusicSession {

  public enum SessionState {
    PLAY, STOP, PAUSE
  }

  private long id;

  private String host;

  private String name;

  private SessionState sessionState;

  private Instant songStarted;

  private Duration savedProgression;

  private Queue queue;

  public MusicSession() {
  }

  public MusicSession(String host, String name) {
    this.host = host;
    this.name = name;
    this.queue = new Queue();
    this.savedProgression = Duration.ZERO;
  }

  public MusicSession(String host, String name, Playlist playlist) {
    this(host, name);
    this.queue = new Queue(playlist);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonIgnore
  public Optional<Song> getCurrentSong() {
    if (this.queue.getCurrentSong() == null) {
      this.nextSong();
    }
    return Optional.ofNullable(queue.getCurrentSong());

  }

  @JsonIgnore
  public Duration getSongStartOffset() {
    Duration duration = Duration.ZERO;
    if (songStarted != null) {
      duration = duration.plus(Duration.between(songStarted, Instant.now()));
    }
    if (duration != null) {
      duration = duration.plus(savedProgression);
    }
    return duration;
  }

  @JsonIgnore
  public void nextSong() {
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
    savedProgression = Duration.ZERO;
  }

  @JsonIgnore
  public void addSongs(List<Song> songs) {
    queue.getQueuedSongs().addAll(songs);
  }

  public void setState(SessionState state) {
    Instant now = Instant.now();
    if (state.equals(SessionState.PAUSE) && songStarted != null) {
      this.savedProgression = savedProgression.plus(Duration.between(songStarted, now));
      songStarted = null;
    }
    if (state.equals(SessionState.PLAY)) {
      songStarted = now.plus(1, ChronoUnit.SECONDS);
    }
    sessionState = state;
  }

  public SessionState getSessionState() {
    return sessionState;
  }
}
