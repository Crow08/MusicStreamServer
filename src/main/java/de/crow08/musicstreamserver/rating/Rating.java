package de.crow08.musicstreamserver.rating;

import de.crow08.musicstreamserver.media.Media;
import de.crow08.musicstreamserver.users.User;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;


@Entity
public class Rating {

  @EmbeddedId
  private RatingId id;

  private short ratingValue;

  @Deprecated
  public Rating() {
  }

  public Rating(User user, Media media, short ratingValue) {
    this.id = new RatingId(user, media);
    this.ratingValue = ratingValue;
  }

  public RatingId getId() {
    return id;
  }

  public void setId(RatingId id) {
    this.id = id;
  }

  public short getRatingValue() {
    return ratingValue;
  }

  public void setRatingValue(short ratingValue) {
    this.ratingValue = ratingValue;
  }
}
