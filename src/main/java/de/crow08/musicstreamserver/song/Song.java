package de.crow08.musicstreamserver.song;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.crow08.musicstreamserver.artist.Artist;
import de.crow08.musicstreamserver.playlists.Playlist;
import org.springframework.context.annotation.Lazy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import java.util.List;

@Entity
public class Song {

  @Id
  @GeneratedValue
  private long id;

  @Column(nullable = false)
  private String title;

  @ManyToOne
  private Artist artist;

  @Column(nullable = false)
  private String path;

  @ManyToMany(mappedBy = "songs")
  @Lazy
  @JsonIgnore
  private List<Playlist> playlists;

  public Song() {
  }

  public Song(String title, String path, Artist artist) {
    this.title = title;
    this.path = path;
    this.artist = artist;
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

  public Artist getArtist() {
    return artist;
  }

  public void setArtist(Artist artist) {
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
