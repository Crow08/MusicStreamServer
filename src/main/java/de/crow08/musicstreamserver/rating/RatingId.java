package de.crow08.musicstreamserver.rating;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RatingId implements Serializable {
  @Column(name = "user_id")
  private long userId;
  @Column(name = "song_id")
  private long songId;

  public RatingId() {
  }

  public RatingId(long userId, long songId) {
    this.userId = userId;
    this.songId = songId;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public long getSongId() {
    return songId;
  }

  public void setSongId(long songId) {
    this.songId = songId;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RatingId) {
      RatingId ratingId = (RatingId) obj;
      return ratingId.userId == this.userId && ratingId.songId == songId;
    }
    return false;
  }
}
