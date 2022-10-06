package de.crow08.musicstreamserver.sessions;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SessionRepository implements Repository<Session, Long> {

  private final Map<Long, Session> repo = new HashMap<>();
  private long idSequencer = 1;

  public Optional<Session> findById(long id) {
    return Optional.ofNullable(repo.get(id));
  }

  public void save(Session session) {
    long sessionId = getSessionId(session);
    repo.put(sessionId, session);
  }

  public Iterable<Session> findAll() {
    return repo.values();
  }

  private long getSessionId(Session session) {
    long sessionId = session.getId();
    if (sessionId == 0) {
      sessionId = idSequencer++;
      session.setId(sessionId);
    } else if (sessionId >= idSequencer) {
      idSequencer = sessionId + 1;
    }
    return sessionId;
  }
}
