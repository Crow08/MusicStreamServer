package de.crow08.musicstreamserver.model.media;

public class MinimalMedia {
  long id;
  String title;

  MediaType type;

  public MinimalMedia() {
  }

  public MinimalMedia(long id, String title, MediaType type) {
    this.id = id;
    this.title = title;
    this.type = type;
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

  public MediaType getType() {
    return type;
  }

  public void setType(MediaType type) {
    this.type = type;
  }
}
