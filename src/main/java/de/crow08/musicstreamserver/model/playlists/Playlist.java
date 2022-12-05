package de.crow08.musicstreamserver.model.playlists;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.crow08.musicstreamserver.model.media.Media;
import de.crow08.musicstreamserver.model.users.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity
public class Playlist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne
  private User author;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "media_playlist",
      joinColumns = @JoinColumn(name = "playlist_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "media_id", referencedColumnName = "id"))
  private List<Media> media;

  public Playlist() {
  }

  public Playlist(String name, User author) {
    this.name = name;
    this.author = author;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  @JsonIgnore
  public List<Media> getSongs() {
    return media;
  }

  @JsonIgnore
  public void setSongs(List<Media> songs) {
    this.media = songs;
  }
}
