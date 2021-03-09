package de.crow08.musicstreamserver.artist;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @PostMapping(path = "/")
  public @ResponseBody long creatNewArtist(@RequestBody String name) {
    Artist artist = new Artist(name);
    artistRepository.save(artist);
    return artist.getId();
  }
}
