package de.crow08.musicstreamserver.song;

import com.neverpile.urlcrypto.PreSignedUrlEnabled;
import de.crow08.musicstreamserver.artist.Artist;
import de.crow08.musicstreamserver.artist.ArtistRepository;
import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.playlists.PlaylistRepository;
import de.crow08.musicstreamserver.utils.TrimmedAudioInputStream;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/songs")
public class SongResource {

  private final SongRepository songRepository;

  private final ArtistRepository artistRepository;

  private final PlaylistRepository playlistRepository;

  @Autowired
  public SongResource(SongRepository songRepository, ArtistRepository artistRepository, PlaylistRepository playlistRepository) {
    this.songRepository = songRepository;
    this.artistRepository = artistRepository;
    this.playlistRepository = playlistRepository;
  }

  @PostMapping("/")
  public @ResponseBody ResponseEntity<Resource> uploadSong(@RequestParam("file") MultipartFile[] files, @RequestParam("artistId") long artistId, @RequestParam("playlistId") long playlistId) throws IOException {


    for (MultipartFile mpFile : files) {
      Optional<Artist> artist = artistRepository.findById(artistId);
      if(artist.isPresent()) {
        String fileName = Objects.requireNonNull(mpFile.getOriginalFilename());
        String songPath = "songs/" + artistId;
        Path fullPath = Paths.get("src", "main", "resources", songPath);
        Files.createDirectories(fullPath);
        File file = new File(Paths.get("src", "main", "resources", "songs", Long.toString(artistId)).toAbsolutePath().toString(), fileName);
        if (!file.createNewFile()) {
          return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        mpFile.transferTo(file);

        Song song = new Song(fileName, songPath + "/" + fileName, artist.get());
        songRepository.save(song);

        if (playlistId != 0) {
          playlistRepository.findById(playlistId).ifPresent(playlist -> {
            playlist.getSongs().add(song);
            playlistRepository.save(playlist);
          });
        }
      }
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<Song> getSessions() {
    return songRepository.findAll();
  }

  @GetMapping("/{id}")
  public @ResponseBody Optional<Song> getSong(@PathVariable String id) {
    return songRepository.findById(Long.parseLong(id));
  }

  @GetMapping("/{id}/data/{offset}")
  @PreSignedUrlEnabled
  public @ResponseBody ResponseEntity<Resource> getSongData(@PathVariable String id, @PathVariable String offset) throws IOException, UnsupportedAudioFileException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException {
    Optional<Song> song = this.songRepository.findById(Long.parseLong(id));
    long offsetMillis = Long.parseLong(offset);
    if (song.isPresent()) {
      File origFile = ResourceUtils.getFile("classpath:" + song.get().getPath());
      File outFile;
      if (song.get().getPath().endsWith(".wav")) {
        outFile = cutWaveFile(origFile, offsetMillis);
      } else if (song.get().getPath().endsWith(".mp3")) {
        outFile = cutMP3File(origFile, offsetMillis);
      } else {
        return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
      }
      InputStreamResource stream = new InputStreamResource(new FileInputStream(outFile));

      return ResponseEntity.ok()
          .contentLength(outFile.length())
          .contentType(MediaType.valueOf("audio/mpeg"))
          .header("Accept-Ranges", "bytes")
          .body(stream);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/{id}/data")
  @PreSignedUrlEnabled
  public @ResponseBody ResponseEntity<Resource> getSongData(@PathVariable String id) throws IOException, UnsupportedAudioFileException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException {
    Optional<Song> song = this.songRepository.findById(Long.parseLong(id));
    if (song.isPresent()) {
      File origFile = ResourceUtils.getFile("classpath:" + song.get().getPath());
      InputStreamResource stream = new InputStreamResource(new FileInputStream(origFile));
      return ResponseEntity.ok()
          .contentLength(origFile.length())
          .contentType(MediaType.valueOf("audio/mpeg"))
          .header("Accept-Ranges", "bytes")
          .body(stream);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  private File cutMP3File(File origFile, long start) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, UnsupportedAudioFileException {
    File outFile;
    MP3File f = (MP3File) AudioFileIO.read(origFile);
    AudioInputStream fullSong = AudioSystem.getAudioInputStream(origFile);
    AudioInputStream trimmedSong = new TrimmedAudioInputStream(fullSong, start, f.getMP3AudioHeader());
    outFile = new File("buffer.mp3");
    OutputStream outStream = new FileOutputStream(outFile);
    outStream.write(trimmedSong.readAllBytes());
    outStream.flush();
    outStream.close();
    return outFile;
  }

  private File cutWaveFile(File origFile, long start) throws UnsupportedAudioFileException, IOException {
    File outFile;
    AudioInputStream fullSong = AudioSystem.getAudioInputStream(origFile);
    AudioInputStream trimmedSong = new TrimmedAudioInputStream(fullSong, start);
    outFile = new File("buffer.wav");
    AudioSystem.write(trimmedSong, AudioFileFormat.Type.WAVE, outFile);
    return outFile;
  }
}
