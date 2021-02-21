package de.crow08.musicstreamserver.song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.crow08.musicstreamserver.sessions.MusicSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

@RestController
@RequestMapping("/songs")
public class SongResource {

  private final SongRepository songRepository;

  @Autowired
  public SongResource(SongRepository songRepository) {
    this.songRepository = songRepository;
  }
  
  @GetMapping("/all")
  public @ResponseBody Iterable<Song> getSessions() {
    return songRepository.findAll();
  }

  @GetMapping("/{id}")
  public @ResponseBody Optional<Song> getSong(@PathVariable int id) {
    return songRepository.findById(id);
  }

  @GetMapping("/{id}/data")
  public @ResponseBody ResponseEntity<Resource> getSongData(@PathVariable int id) throws FileNotFoundException {
    Optional<Song> song = this.songRepository.findById(id);
    if (song.isPresent()) {
      File file = ResourceUtils.getFile("classpath:"+song.get().getPath());
      InputStreamResource stream = new InputStreamResource(new FileInputStream(file));
      return ResponseEntity.ok().contentLength(file.length()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(stream);
    }else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
