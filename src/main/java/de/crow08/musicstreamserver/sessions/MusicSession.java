package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.queue.Queue;
import de.crow08.musicstreamserver.song.Song;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MusicSession {

  private UUID id;

  private String host;

  private String name;

  private Instant songStartedTime;

  private Queue queue;

  public MusicSession() {
  }

  public MusicSession(String host, String name) {
    this.host = host;
    this.name = name;
  }

  public MusicSession(String host, String name, Playlist playlist) {
    this(host, name);
    queue = new Queue(playlist);
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
    return Optional.ofNullable(queue.getCurrentSong());
  }

  public Optional<Instant> getcurrentSongStartedTime() {
    return Optional.ofNullable(songStartedTime);
  }

  public Optional<Song> nextSong() {
    Song currentSong = queue.getCurrentSong();
    if (currentSong != null) {
      queue.getHistorySongs().add(currentSong);
    }
    if(queue.getQueuedSongs().size() > 0){
      queue.setCurrentSong(queue.getQueuedSongs().get(0));
      queue.getQueuedSongs().remove(0);
    } else {
      queue.setCurrentSong(null);
    }
    return getCurrentSong();
  }

  public void addSongs(List<Song> songs) {
    queue.getQueuedSongs().addAll(songs);
  }
}
