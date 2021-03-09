package de.crow08.musicstreamserver.rating;

import de.crow08.musicstreamserver.song.Song;
import de.crow08.musicstreamserver.users.User;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
public class Rating {

  @EmbeddedId
  private RatingId id;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "song_id", insertable = false, updatable = false)
  private Song song;

  private short ratingValue;

  public Rating() {
  }

  public Rating(User user, Song song, short ratingValue) {
    this.user = user;
    this.song = song;
    this.ratingValue = ratingValue;
  }

  public RatingId getId() {
    return id;
  }

  public void setId(RatingId id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Song getSong() {
    return song;
  }

  public void setSong(Song songs) {
    this.song = songs;
  }

  public long getRatingValue() {
    return ratingValue;
  }

  public void setRatingValue(short ratingValue) {
    this.ratingValue = ratingValue;
  }
}
