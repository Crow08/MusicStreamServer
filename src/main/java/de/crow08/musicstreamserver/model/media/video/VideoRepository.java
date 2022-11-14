package de.crow08.musicstreamserver.model.media.video;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface VideoRepository extends PagingAndSortingRepository<Video, Long> {
}
