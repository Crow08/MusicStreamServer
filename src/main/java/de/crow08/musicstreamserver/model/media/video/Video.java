package de.crow08.musicstreamserver.model.media.video;

import de.crow08.musicstreamserver.model.season.Season;
import de.crow08.musicstreamserver.model.series.Series;
import de.crow08.musicstreamserver.model.media.Media;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class Video extends Media {

  @ManyToOne(fetch = FetchType.EAGER)
  private Series series;

  @ManyToOne(fetch = FetchType.EAGER)
  private Season season;

  @Deprecated
  public Video() {
  }

  public Video(long id) {
    super(id);
  }

  public Video(String title, String uri) {
    super(title, uri);
  }
}
