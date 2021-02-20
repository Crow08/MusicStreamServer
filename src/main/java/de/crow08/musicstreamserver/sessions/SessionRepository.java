package de.crow08.musicstreamserver.sessions;

import org.springframework.data.repository.CrudRepository;

public interface SessionRepository extends CrudRepository<MusicSession, Integer> {
}
