package de.crow08.musicstreamserver.artist;

import de.crow08.musicstreamserver.song.Song;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class Artist {

  @Id
  @GeneratedValue
  private long id;

  @Column(nullable = false, unique = true)
  private String name;

  @OneToMany(mappedBy="artist")
  private Set<Song> songs;

  public Artist() {
  }

  public Artist(String name) {
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
