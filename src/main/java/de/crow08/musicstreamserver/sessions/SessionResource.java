package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.authentication.AuthenticatedUser;
import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.playlists.PlaylistRepository;
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

  final SessionController sessionController;
  private final SessionRepository sessionRepository;
  private final PlaylistRepository playlistRepository;

  @Autowired
  public SessionResource(SessionRepository sessionRepository, PlaylistRepository playlistRepository, SessionController sessionController) {
    this.sessionRepository = sessionRepository;
    this.playlistRepository = playlistRepository;
    this.sessionController = sessionController;
  }

  @GetMapping("/{id}")
  public @ResponseBody Optional<Session> getSession(@PathVariable String id) {
    return sessionRepository.findById(Long.parseLong(id));
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<Session> getSessions() {
    return sessionRepository.findAll();
  }

  @PutMapping(path = "/")
  public @ResponseBody long creatNewSession(@RequestBody String name) {
    AuthenticatedUser user = ((AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    Session session = new Session(user.getUsername(), name);
    sessionRepository.save(session);
    return session.getId();
  }

  @PutMapping(path = "/{sessionId}/addpl")
  public @ResponseBody String creatNewSession(@PathVariable String sessionId, @RequestBody String playlistId) {
    Optional<Session> session = sessionRepository.findById(Long.parseLong(sessionId));
    Optional<Playlist> playlist = playlistRepository.findById(Long.parseLong(playlistId));
    if (session.isPresent() && playlist.isPresent()) {
      sessionController.addSongs(session.get(), playlist.get().getSongs());
    }
    return "OK";
  }
}
