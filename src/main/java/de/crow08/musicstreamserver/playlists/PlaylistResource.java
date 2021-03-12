package de.crow08.musicstreamserver.playlists;

import de.crow08.musicstreamserver.authentication.AuthenticatedUser;
import de.crow08.musicstreamserver.users.User;
import de.crow08.musicstreamserver.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/playlists")
public class PlaylistResource {

  private final PlaylistRepository playlistRepository;
  private final UserRepository userRepository;

  @Autowired
  public PlaylistResource(PlaylistRepository playlistRepository, UserRepository userRepository) {
    this.playlistRepository = playlistRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<Playlist> getPlaylists() {
    return playlistRepository.findAll();
  }

  @GetMapping("/{id}")
  public @ResponseBody Optional<Playlist> getSong(@PathVariable String id) {
    return playlistRepository.findById(Long.parseLong(id));
  }

  @PostMapping(path = "/")
  public @ResponseBody long createNewPlaylist(@RequestBody String name) throws Exception {
    AuthenticatedUser authUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new Exception("User Not found!"));
    Playlist playlist = new Playlist(name, user);
    playlistRepository.save(playlist);
    return playlist.getId();
  }
}
