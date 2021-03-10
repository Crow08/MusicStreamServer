package de.crow08.musicstreamserver.song;

import de.crow08.musicstreamserver.album.Album;
import de.crow08.musicstreamserver.artist.Artist;
import de.crow08.musicstreamserver.genre.Genre;
import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.tag.Tag;
import org.springframework.context.annotation.Lazy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity
public class Song {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  private String title;

  @ManyToOne
  private Artist artist;

  @ManyToOne
  private Album album;

  @ManyToMany
  @JoinTable(name = "song_genre",
      joinColumns = @JoinColumn(name = "song_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "id"))
  private List<Genre> genre;

  @ManyToMany
  @JoinTable(name = "song_tag",
      joinColumns = @JoinColumn(name = "song_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
  private List<Tag> tags;

  @Column(nullable = false)
  private String path;

  @ManyToMany(mappedBy = "songs")
  @Lazy
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

  public Album getAlbum() {
    return album;
  }

  public void setAlbum(Album album) {
    this.album = album;
  }

  public List<Genre> getGenre() {
    return genre;
  }

  public void setGenre(List<Genre> genre) {
    this.genre = genre;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }

  public List<Tag> getTag() {
    return tags;
  }

  public void setTag(List<Tag> tags) {
    this.tags = tags;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<Playlist> getPlaylists() {
    return playlists;
  }

  public void setPlaylists(List<Playlist> playlists) {
    this.playlists = playlists;
  }
}
