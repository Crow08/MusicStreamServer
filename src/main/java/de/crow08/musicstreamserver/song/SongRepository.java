package de.crow08.musicstreamserver.song;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends CrudRepository<Song, Long> {
  List<Song> findByTitleContains(String keyword);

  @Query(nativeQuery = true, value = "SELECT song.* FROM song INNER JOIN artist ON song.artist_id=artist.id WHERE artist.id IN :keyword")
  List<Song> findByArtist(@Param("keyword") String[] keyword);
  
  @Query(nativeQuery = true, value = "SELECT song.* FROM song INNER JOIN song_genre on song.id = song_genre.song_id INNER JOIN genre on song_genre.genre_id = genre.id WHERE genre.id IN :keyword")
  List<Song> findByGenre(@Param("keyword") String[] keyword);
}
