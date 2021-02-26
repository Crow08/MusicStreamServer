package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.song.Song;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class MusicSession {

  private UUID id;

  private String host;

  private String name;

  private Song currentSong;

  private Instant songStartedTime;

  public MusicSession() {
  }

  public MusicSession(String host, String name) {
    this.host = host;
    this.name = name;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
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

  public Optional<Song> getCurrentSong() {
    return Optional.ofNullable(currentSong);
  }

  public Optional<Instant> getcurrentSongStartedTime() {
    return Optional.ofNullable(songStartedTime);
  }

  public void nextSong() {
  }
}
