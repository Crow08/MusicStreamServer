package de.crow08.musicstreamserver.artist;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/artists")
public class ArtistResource {

  private final ArtistRepository artistRepository;

  public ArtistResource(ArtistRepository artistRepository) {
    this.artistRepository = artistRepository;
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<Artist> getSessions() {
    return artistRepository.findAll();
  }
}
