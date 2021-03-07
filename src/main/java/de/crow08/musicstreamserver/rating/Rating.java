package de.crow08.musicstreamserver.rating;

import de.crow08.musicstreamserver.song.Song;
import de.crow08.musicstreamserver.users.User;

import javax.persistence.Id;
import javax.persistence.OneToOne;

public class Rating {

  @Id
  @OneToOne
  private User user;

  @Id
  @OneToOne
  private Song songs;

  private long ratingValue;

  public Rating() {
  }

  public Rating(User user, Song songs, long ratingValue) {
    this.user = user;
    this.songs = songs;
    this.ratingValue = ratingValue;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Song getSongs() {
    return songs;
  }

  public void setSongs(Song songs) {
    this.songs = songs;
  }

  public long getRatingValue() {
    return ratingValue;
  }

  public void setRatingValue(long ratingValue) {
    this.ratingValue = ratingValue;
  }
}
