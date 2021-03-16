package de.crow08.musicstreamserver.song;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SongRepository extends CrudRepository<Song, Long> {
  List<Song> findByTitleContains(String keyword);
  @Query(nativeQuery = true, value = "SELECT song.path, song.title, artist.name, artist.id, song.album_id, song.artist_id FROM song INNER JOIN artist ON song.artist_id=artist.id WHERE artist.name LIKE CONCAT('%',:keyword,'%')")
  List<Song> findByArtist(@Param("keyword") String keyword);
}
