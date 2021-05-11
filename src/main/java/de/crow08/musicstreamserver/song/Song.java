package de.crow08.musicstreamserver.song;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.crow08.musicstreamserver.album.Album;
import de.crow08.musicstreamserver.artist.Artist;
import de.crow08.musicstreamserver.genre.Genre;
import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.tag.Tag;

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
import java.util.Set;

@Entity
public class Song {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  private String title;

  @ManyToOne(fetch = FetchType.EAGER)
  private Artist artist;

  @ManyToOne(fetch = FetchType.EAGER)
  private Album album;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "song_genre",
      joinColumns = @JoinColumn(name = "song_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "id"))
  private Set<Genre> genres;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "song_tag",
      joinColumns = @JoinColumn(name = "song_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
  private Set<Tag> tags;

  @Column(nullable = false)
  private String path;

  @ManyToMany(mappedBy = "songs", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<Playlist> playlists;

  @Deprecated
  public Song() {
  }

  public Song(long id) {
    this.id = id;
  }

  public Song(String title, String path) {
    this.title = title;
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

  public Artist getArtist() {
    return artist;
  }

  public void setArtist(Artist artist) {
    this.artist = artist;
  }

  public Album getAlbum() {
    return album;
  }

  public void setAlbum(Album album) {
    this.album = album;
  }

  public Set<Genre> getGenres() {
    return genres;
  }

  public void setGenres(Set<Genre> genre) {
    this.genres = genre;
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public void setTags(Set<Tag> tags) {
    this.tags = tags;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Set<Playlist> getPlaylists() {
    return playlists;
  }

  public void setPlaylists(Set<Playlist> playlists) {
    this.playlists = playlists;
  }
}
