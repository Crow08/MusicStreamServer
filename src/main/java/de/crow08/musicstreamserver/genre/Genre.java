package de.crow08.musicstreamserver.genre;

import de.crow08.musicstreamserver.song.Song;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class Genre {

  @Id
  @GeneratedValue
  private long id;

  @Column(nullable = false, unique = true)
  private String name;

  @ManyToMany(mappedBy = "genre")
  private List<Song> songs;

  public Genre() {
  }

  public Genre(String name) {
    this.name = name;
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
}
