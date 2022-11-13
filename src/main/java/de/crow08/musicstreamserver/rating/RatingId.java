package de.crow08.musicstreamserver.rating;

import de.crow08.musicstreamserver.media.Media;
import de.crow08.musicstreamserver.users.User;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class RatingId implements Serializable {
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "song_id", insertable = false, updatable = false)
  private Media media;

  public RatingId() {
  }

  public RatingId(User user, Media media) {
    this.user = user;
    this.media = media;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Media getSong() {
    return media;
  }

  public void setSong(Media media) {
    this.media = media;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RatingId ratingId) {
      return ratingId.user.getId() == this.user.getId() && ratingId.media.getId() == media.getId();
    }
    return false;
  }
}
