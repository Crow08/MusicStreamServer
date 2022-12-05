package de.crow08.musicstreamserver.model.queue;

import de.crow08.musicstreamserver.model.media.Media;
import de.crow08.musicstreamserver.model.playlists.Playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Queue {

  private UUID id;

  private Media currentMedia;

  private List<Media> queuedMedia;

  private List<Media> historyMedia;

  public Queue() {
    queuedMedia = new ArrayList<>();
    historyMedia = new ArrayList<>();
  }

  public Queue(Playlist playlist) {
    queuedMedia = playlist.getSongs();
    historyMedia = new ArrayList<>();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Media getCurrentMedia() {
    return currentMedia;
  }

  public void setCurrentMedia(Media currentMedia) {
    this.currentMedia = currentMedia;
  }

  public List<Media> getQueuedMedia() {
    return queuedMedia;
  }

  public void setQueuedSongs(List<Media> queuedMedia) {
    this.queuedMedia = queuedMedia;
  }

  public List<Media> getHistoryMedia() {
    return historyMedia;
  }

  public void setHistorySongs(List<Media> historyMedia) {
    this.historyMedia = historyMedia;
  }
}
