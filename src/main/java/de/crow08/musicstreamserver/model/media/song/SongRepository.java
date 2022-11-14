package de.crow08.musicstreamserver.model.media.song;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface SongRepository extends PagingAndSortingRepository<Song, Long> {
}
