package de.crow08.musicstreamserver.model.rating;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RatingRepository extends CrudRepository<Rating, RatingId> {
  @Query(value = "SELECT rating_value FROM `rating` WHERE song_id = ?1", nativeQuery = true)
  List<Short> findRatingsBySongId(long songId);
}
