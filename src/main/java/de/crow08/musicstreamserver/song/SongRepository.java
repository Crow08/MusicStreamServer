package de.crow08.musicstreamserver.song;

import org.springframework.data.repository.CrudRepository;

public interface SongRepository extends CrudRepository<Song, String> {
}
