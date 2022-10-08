package de.crow08.musicstreamserver.song;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends PagingAndSortingRepository<Song, Long> {
  List<Song> findByTitleContains(String keyword);

  @Query(nativeQuery = true, value = "SELECT DISTINCT song.* FROM song LEFT JOIN song_genre ON song.id = song_genre.song_id WHERE artist_id IN :keyword")
  List<Song> findByArtist(@Param("keyword") String[] keyword);

  @Query(nativeQuery = true, value = "SELECT DISTINCT song.* FROM song LEFT JOIN song_genre ON song.id = song_genre.song_id WHERE genre_id IN :keyword")
  List<Song> findByGenre(@Param("keyword") String[] keyword);
}
