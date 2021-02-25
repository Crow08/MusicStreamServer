package de.crow08.musicstreamserver.song;

import com.neverpile.urlcrypto.PreSignedUrlEnabled;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
  @PreSignedUrlEnabled
  public @ResponseBody ResponseEntity<Resource> getSongData(@PathVariable int id) throws IOException, UnsupportedAudioFileException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException {
    Optional<Song> song = this.songRepository.findById(id);
    if (song.isPresent()) {
      File origFile = ResourceUtils.getFile("classpath:" + song.get().getPath());
      File outFile;
      if (song.get().getPath().endsWith(".wav")) {
        outFile = cutWaveFile(origFile, 60000);
      } else if (song.get().getPath().endsWith(".mp3")) {
        outFile = cutMP3File(origFile, 60000);
      } else {
        outFile = origFile;
      }
      InputStreamResource stream = new InputStreamResource(new FileInputStream(outFile));

      return ResponseEntity.ok()
          .contentLength(outFile.length())
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header("Accept-Ranges", "bytes")
          .body(stream);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  private File cutMP3File(File origFile, int start) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, UnsupportedAudioFileException {
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

  private File cutWaveFile(File origFile, int start) throws UnsupportedAudioFileException, IOException {
    File outFile;
    AudioInputStream fullSong = AudioSystem.getAudioInputStream(origFile);
    AudioInputStream trimmedSong = new TrimmedAudioInputStream(fullSong, start);
    outFile = new File("buffer.wav");
    AudioSystem.write(trimmedSong, AudioFileFormat.Type.WAVE, outFile);
    return outFile;
  }
}
