package de.crow08.musicstreamserver.playlists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.Optional;

@RestController
@RequestMapping("/playlist")
public class PlaylistResource {

  private final PlaylistRepository playlistRepository;

  @Autowired
  public PlaylistResource(PlaylistRepository playlistRepository) {
    this.playlistRepository = playlistRepository;
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<Playlist> getSessions() {
    return playlistRepository.findAll();
  }

  @GetMapping("/{id}")
  public @ResponseBody Optional<Playlist> getSong(@PathVariable String id) {
    return playlistRepository.findById(id);
  }

}
