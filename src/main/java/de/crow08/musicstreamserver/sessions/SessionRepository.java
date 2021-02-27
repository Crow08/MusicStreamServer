package de.crow08.musicstreamserver.sessions;

import org.springframework.data.repository.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SessionRepository implements Repository<MusicSession, Long> {

  private long idSequencer = 1;

  private Map<Long, MusicSession> repo = new HashMap<>();

  public Optional<MusicSession> findById(long id) {
    return Optional.ofNullable(repo.get(id));
  }

  public void save(MusicSession session) {
    long sessionId = getSessionId(session);
    repo.put(sessionId, session);
  }

  public Iterable<MusicSession> findAll() {
    return repo.values();
  }

  private long getSessionId(MusicSession session) {
    long sessionId = session.getId();
    if(sessionId == 0) {
      sessionId = idSequencer++;
      session.setId(sessionId);
    } else if(sessionId >= idSequencer){
      idSequencer = sessionId + 1;
    }
    return sessionId;
  }
}
