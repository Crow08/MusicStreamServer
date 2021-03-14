package de.crow08.musicstreamserver.song;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface SongRepository extends CrudRepository<Song, Long> {
  List<Song> findByTitleContains(String keyword);
}
