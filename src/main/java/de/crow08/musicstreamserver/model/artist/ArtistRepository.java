package de.crow08.musicstreamserver.model.artist;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ArtistRepository extends CrudRepository<Artist, Long> {

  List<Artist> findByName(String keyword);
}


