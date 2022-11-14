package de.crow08.musicstreamserver.model.media.song;

import de.crow08.musicstreamserver.model.album.Album;
import de.crow08.musicstreamserver.model.artist.Artist;
import de.crow08.musicstreamserver.model.genre.Genre;
import de.crow08.musicstreamserver.model.media.Media;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.Set;

@Entity
public class Song extends Media {

  @ManyToOne(fetch = FetchType.EAGER)
  private Artist artist;

  @ManyToOne(fetch = FetchType.EAGER)
  private Album album;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "song_genre",
      joinColumns = @JoinColumn(name = "song_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "id"))
  private Set<Genre> genres;

  private boolean spotify = false;

  @Deprecated
  public Song() {
  }

  public Song(long id) {
    super(id);
  }

  public Song(String title, String uri) {
    super(title, uri);
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

  public boolean isSpotify() {
    return spotify;
  }

  public void setSpotify(boolean spotify) {
    this.spotify = spotify;
  }
}
