package de.crow08.musicstreamserver.song;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.crow08.musicstreamserver.playlists.Playlist;
import org.springframework.context.annotation.Lazy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class Song {

  @Id
  @GeneratedValue
  private long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = true)
  private String artist;

  @Column(nullable = false)
  private String path;

  @ManyToMany(mappedBy = "songs")
  @Lazy
  @JsonIgnore
  private List<Playlist> playlists;

  public Song() {
  }

  public Song(String title, String artist, String path) {
    this.title = title;
    this.artist = artist;
    this.path = path;
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

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @JsonIgnore
  public List<Playlist> getPlaylists() {
    return playlists;
  }

  @JsonIgnore
  public void setPlaylists(List<Playlist> playlists) {
    this.playlists = playlists;
  }
}
