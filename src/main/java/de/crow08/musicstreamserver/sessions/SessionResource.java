package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.users.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/sessions")
public class SessionResource {

  private final SessionRepository sessionRepository;

  @Autowired
  public SessionResource(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  @GetMapping("/{id}")
  public @ResponseBody Optional<MusicSession> getSession(@PathVariable int id) {
    return sessionRepository.findById(id);
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<MusicSession> getSessions() {
    return sessionRepository.findAll();
  }

  @PutMapping(path = "/")
  public @ResponseBody String creatNewSession(@RequestBody String name) {
    AuthenticatedUser user = ((AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    MusicSession session = new MusicSession(user.getUsername(), name);
    sessionRepository.save(session);
    return session.getId().toString();
  }
}
