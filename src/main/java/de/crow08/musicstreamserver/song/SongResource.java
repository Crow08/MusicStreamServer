package de.crow08.musicstreamserver.song;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neverpile.urlcrypto.PreSignedUrlEnabled;
import de.crow08.musicstreamserver.album.Album;
import de.crow08.musicstreamserver.album.AlbumRepository;
import de.crow08.musicstreamserver.artist.Artist;
import de.crow08.musicstreamserver.artist.ArtistRepository;
import de.crow08.musicstreamserver.genre.Genre;
import de.crow08.musicstreamserver.genre.GenreRepository;
import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.playlists.PlaylistRepository;
import de.crow08.musicstreamserver.tag.Tag;
import de.crow08.musicstreamserver.tag.TagRepository;
import de.crow08.musicstreamserver.utils.TrimmedAudioInputStream;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/songs")
public class SongResource {

  private final SongRepository songRepository;

  private final ArtistRepository artistRepository;

  private final PlaylistRepository playlistRepository;

  private final GenreRepository genreRepository;
  private final AlbumRepository albumRepository;
  private final TagRepository tagRepository;

  private final Path storagePath;

  @Autowired
  public SongResource(SongRepository songRepository,
                      ArtistRepository artistRepository,
                      PlaylistRepository playlistRepository,
                      GenreRepository genreRepository,
                      AlbumRepository albumRepository,
                      TagRepository tagRepository,
                      @Value("${client.storage-path}") String clientStoragePath) {
    this.songRepository = songRepository;
    this.artistRepository = artistRepository;
    this.playlistRepository = playlistRepository;
    this.genreRepository = genreRepository;
    this.albumRepository = albumRepository;
    this.tagRepository = tagRepository;
    storagePath = Paths.get(clientStoragePath);
  }

  @PostMapping("/")
  @Transactional
  public @ResponseBody ResponseEntity<Resource> uploadSong(@RequestParam("files") MultipartFile[] files,
                                                           @RequestParam("data") String data) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode dataNode = mapper.readTree(data);

    long artistId = 1; // 1 is Fallback
    if (dataNode.get("artistId") != null && dataNode.get("artistId").isNumber()) {
      artistId = dataNode.get("artistId").longValue();
    }

    long albumId = 1; // 1 is Fallback
    if (dataNode.get("albumId") != null && dataNode.get("albumId").isNumber()) {
      albumId = dataNode.get("albumId").longValue();
    }

    List<Long> playlistIds = new ArrayList<>();
    if (dataNode.get("playlists").isArray()) {
      for (JsonNode jsonNode : dataNode.get("playlists")) {
        playlistIds.add(jsonNode.asLong());
      }
    }

    List<Long> genreIds = new ArrayList<>();
    if (dataNode.get("genres").isArray()) {
      for (JsonNode jsonNode : dataNode.get("genres")) {
        genreIds.add(jsonNode.asLong());
      }
    }

    List<Long> tagIds = new ArrayList<>();
    if (dataNode.get("tags").isArray()) {
      for (JsonNode jsonNode : dataNode.get("tags")) {
        tagIds.add(jsonNode.asLong());
      }
    }

    Optional<Artist> artist = artistRepository.findById(artistId);
    Optional<Album> album = albumRepository.findById(albumId);
    Iterable<Genre> genres = genreRepository.findAllById(genreIds);
    Iterable<Tag> tags = tagRepository.findAllById(tagIds);

    for (MultipartFile mpFile : files) {
      String fileName = Objects.requireNonNull(mpFile.getOriginalFilename());
      String songPath = "/songs/" + artistId;
      Path fullPath = Paths.get(this.storagePath.toString(), songPath);
      // Create new song
      Song song = new Song(fileName, songPath + "/" + fileName);
      // Add artist
      song.setArtist(artist.orElse(null));
      // Add album
      song.setAlbum(album.orElse(null));
      // Add genres
      Set<Genre> genresList = new HashSet<>();
      genres.forEach(genresList::add);
      song.setGenres(genresList);
      // Add tags
      Set<Tag> tagsList = new HashSet<>();
      tags.forEach(tagsList::add);
      song.setTags(tagsList);
      // Save song
      songRepository.save(song);

      //write Song file
      Files.createDirectories(fullPath);
      File file = new File(fullPath.toAbsolutePath().toString(), fileName);
      if (!file.createNewFile()) {
        songRepository.delete(song);
        return new ResponseEntity<>(HttpStatus.CONFLICT);
      }
      mpFile.transferTo(file);

      // Add to playlist
      for (long playlistId : playlistIds) {
        if (playlistId > 1) { // Don't add To playlist with ID: 1
          Optional<Playlist> playlist = playlistRepository.findById(playlistId);
          playlist.ifPresent(pl -> {
            if (pl.getSongs() == null) {
              pl.setSongs(new ArrayList<>());
            }
            pl.getSongs().add(song);
            playlistRepository.save(pl);
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
      File origFile = new File(storagePath.toAbsolutePath() + song.get().getPath());
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
      File origFile = new File(this.storagePath.toAbsolutePath() + song.get().getPath());
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

  @GetMapping(path = "/getSongsByKeyword/{keyword}")
  public List<Song> getSongsByKeyword(@PathVariable String keyword) {
    List<Song> songs = songRepository.findByTitleContains(keyword);
    System.out.println(songs.size());
    System.out.println(keyword);
    return songs;
  }

  @GetMapping(path = "/getSongsByArtist/{keyword}")
  public List<Song> getSongsByArtist(@PathVariable String[] keyword) {
    List<Song> songs = songRepository.findByArtist(keyword);
    System.out.println(songs.size());
    System.out.println(keyword);
    return songs;
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
