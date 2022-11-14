package de.crow08.musicstreamserver.model.album;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/albums")
public class AlbumResource {

  private final AlbumRepository albumRepository;

  public AlbumResource(AlbumRepository albumRepository) {
    this.albumRepository = albumRepository;
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<Album> getAlbum() {
    return albumRepository.findAll();
  }

  @PostMapping(path = "/")
  public @ResponseBody long createNewAlbum(@RequestBody String name) {
    Album album = new Album(name);
    albumRepository.save(album);
    return album.getId();
  }
}
