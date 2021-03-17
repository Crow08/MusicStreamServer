package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.playlists.PlaylistRepository;
import de.crow08.musicstreamserver.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

  @PostMapping(path = "/")
  public @ResponseBody long createNewSession(@RequestBody String name) {
    User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    Session session = this.sessionController.createNewSession(name, user);
    sessionRepository.save(session);
    return session.getId();
  }

  @PutMapping(path = "/{sessionId}/addpl")
  public void addSongsToPlaylist(@PathVariable String sessionId, @RequestBody String playlistId) {
    Optional<Session> session = sessionRepository.findById(Long.parseLong(sessionId));
    Optional<Playlist> playlist = playlistRepository.findById(Long.parseLong(playlistId));
    if (session.isPresent() && playlist.isPresent()) {
      sessionController.addSongs(session.get(), playlist.get().getSongs());
    }
  }
}
