package de.crow08.musicstreamserver.playlists;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.crow08.musicstreamserver.song.Song;
import de.crow08.musicstreamserver.users.User;
import org.springframework.context.annotation.Lazy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity
public class Playlist {

  @Id
  @GeneratedValue
  private long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne
  private User author;

  @ManyToMany
  @JoinTable(name = "song_playlist",
      joinColumns = @JoinColumn(name = "playlist_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "song_id", referencedColumnName = "id"))
  @Lazy
  private List<Song> songs;

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
  public List<Song> getSongs() {
    return songs;
  }

  @JsonIgnore
  public void setSongs(List<Song> songs) {
    this.songs = songs;
  }
}
