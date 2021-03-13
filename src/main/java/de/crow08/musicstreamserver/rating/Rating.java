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

  private short ratingValue;

  public Rating() {
  }

  public Rating(User user, Song song, short ratingValue) {
    this.id = new RatingId(user, song);
    this.ratingValue = ratingValue;
  }

  public RatingId getId() {
    return id;
  }

  public void setId(RatingId id) {
    this.id = id;
  }

  public long getRatingValue() {
    return ratingValue;
  }

  public void setRatingValue(short ratingValue) {
    this.ratingValue = ratingValue;
  }
}
