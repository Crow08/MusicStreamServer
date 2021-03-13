package de.crow08.musicstreamserver.rating;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RatingRepository extends CrudRepository<Rating, RatingId> {
  @Query(value="SELECT rating_value FROM `rating` WHERE song_id = ?1", nativeQuery=true)
  public List<Short> findRatingsBySongId(long songId);
}
