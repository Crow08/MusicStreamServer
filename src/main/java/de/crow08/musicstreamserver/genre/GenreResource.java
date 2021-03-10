package de.crow08.musicstreamserver.genre;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public @ResponseBody Iterable<Genre> getGenres() {
    return genreRepository.findAll();
  }

  @PostMapping(path = "/")
  public @ResponseBody long createNewGenre(@RequestBody String name) {
    Genre genre = new Genre(name);
    genreRepository.save(genre);
    return genre.getId();
  }
}
