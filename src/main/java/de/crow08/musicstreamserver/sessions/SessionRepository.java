package de.crow08.musicstreamserver.sessions;

import org.springframework.data.repository.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SessionRepository implements Repository<MusicSession, String> {

  private Map<String, MusicSession> repo = new HashMap<>();

  public Optional<MusicSession> findById(String id) {
    return Optional.ofNullable(repo.get(id));
  }

  public void save(MusicSession session) {
    session.setId(session.getId() == null ? UUID.randomUUID() : session.getId());
    repo.put(session.getId().toString(), session);
  }

  public Iterable<MusicSession> findAll() {
    return repo.values();
  }
}
