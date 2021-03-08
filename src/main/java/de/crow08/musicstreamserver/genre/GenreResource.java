package de.crow08.musicstreamserver.genre;

import de.crow08.musicstreamserver.playlists.Playlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/genres")
public class GenreResource {

  private final GenreRepository genreRepository;

  public GenreResource(GenreRepository genreRepository) {
    this.genreRepository = genreRepository;
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<Genre> getSessions() {
    return genreRepository.findAll();
  }

}
