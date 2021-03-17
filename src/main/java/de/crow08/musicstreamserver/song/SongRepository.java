package de.crow08.musicstreamserver.song;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends CrudRepository<Song, Long> {
  List<Song> findByTitleContains(String keyword);

  @Query(nativeQuery = true, value = "SELECT * FROM song INNER JOIN artist ON song.artist_id=artist.id WHERE artist.name LIKE CONCAT('%',:keyword,'%')")
  List<Song> findByArtist(@Param("keyword") String keyword);
}
