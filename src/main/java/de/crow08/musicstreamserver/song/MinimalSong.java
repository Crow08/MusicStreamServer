package de.crow08.musicstreamserver.song;

public class MinimalSong {
  long id;
  String title;

  public MinimalSong() {
  }

  public MinimalSong(long id, String title) {
    this.id = id;
    this.title = title;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
