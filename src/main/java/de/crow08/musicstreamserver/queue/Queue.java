package de.crow08.musicstreamserver.queue;

import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.song.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Queue {

  private UUID id;

  private Song currentSong;

  private List<Song> queuedSongs;

  private List<Song> historySongs;

  public Queue() {
    queuedSongs = new ArrayList<>();
    historySongs = new ArrayList<>();
  }

  public Queue(Playlist playlist) {
    queuedSongs = playlist.getSongs();
    historySongs = new ArrayList<>();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Song getCurrentSong() {
    return currentSong;
  }

  public void setCurrentSong(Song currentSong) {
    this.currentSong = currentSong;
  }

  public List<Song> getQueuedSongs() {
    return queuedSongs;
  }

  public void setQueuedSongs(List<Song> queuedSongs) {
    this.queuedSongs = queuedSongs;
  }

  public List<Song> getHistorySongs() {
    return historySongs;
  }

  public void setHistorySongs(List<Song> historySongs) {
    this.historySongs = historySongs;
  }
}
