package de.crow08.musicstreamserver.model.album;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AlbumRepository extends CrudRepository<Album, Long> {

  List<Album> findByName(String keyword);
}


