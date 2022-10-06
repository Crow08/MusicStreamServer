package de.crow08.musicstreamserver.rating;

import de.crow08.musicstreamserver.song.Song;
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
  private Song song;

  public RatingId() {
  }

  public RatingId(User user, Song song) {
    this.user = user;
    this.song = song;
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

  public void setSong(Song song) {
    this.song = song;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RatingId ratingId) {
      return ratingId.user.getId() == this.user.getId() && ratingId.song.getId() == song.getId();
    }
    return false;
  }
}
