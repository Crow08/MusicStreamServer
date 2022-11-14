package de.crow08.musicstreamserver.model.media;

public class MinimalMedia {
  long id;
  String title;

  public MinimalMedia() {
  }

  public MinimalMedia(long id, String title) {
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
