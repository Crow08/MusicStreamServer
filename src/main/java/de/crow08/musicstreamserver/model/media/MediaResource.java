package de.crow08.musicstreamserver.model.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neverpile.urlcrypto.PreSignedUrlEnabled;
import de.crow08.musicstreamserver.model.album.Album;
import de.crow08.musicstreamserver.model.album.AlbumRepository;
import de.crow08.musicstreamserver.model.artist.Artist;
import de.crow08.musicstreamserver.model.artist.ArtistRepository;
import de.crow08.musicstreamserver.model.genre.Genre;
import de.crow08.musicstreamserver.model.genre.GenreRepository;
import de.crow08.musicstreamserver.model.media.song.Song;
import de.crow08.musicstreamserver.model.media.song.SongRepository;
import de.crow08.musicstreamserver.model.media.video.VideoRepository;
import de.crow08.musicstreamserver.model.playlists.Playlist;
import de.crow08.musicstreamserver.model.playlists.PlaylistRepository;
import de.crow08.musicstreamserver.model.tag.Tag;
import de.crow08.musicstreamserver.model.tag.TagRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.websocket.server.PathParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.crow08.musicstreamserver.model.media.MediaType.SONG;

@RestController
@RequestMapping("/media")
public class MediaResource {

  private final MediaRepository mediaRepository;

  private final SongRepository songRepository;

  private final VideoRepository videoRepository;

  private final ArtistRepository artistRepository;

  private final PlaylistRepository playlistRepository;

  private final GenreRepository genreRepository;
  private final AlbumRepository albumRepository;
  private final TagRepository tagRepository;

  private final Path storagePath;

  @Autowired
  public MediaResource(MediaRepository mediaRepository,
                       SongRepository songRepository,
                       VideoRepository videoRepository,
                       ArtistRepository artistRepository,
                       PlaylistRepository playlistRepository,
                       GenreRepository genreRepository,
                       AlbumRepository albumRepository,
                       TagRepository tagRepository,
                       @Value("${client.storage-path}") String clientStoragePath) {
    this.mediaRepository = mediaRepository;
    this.songRepository = songRepository;
    this.videoRepository = videoRepository;
    this.artistRepository = artistRepository;
    this.playlistRepository = playlistRepository;
    this.genreRepository = genreRepository;
    this.albumRepository = albumRepository;
    this.tagRepository = tagRepository;
    storagePath = Paths.get(clientStoragePath);
  }

  @PostMapping("/upload/")
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
      mediaRepository.save(song);

      //write Song file
      Files.createDirectories(fullPath);
      File file = new File(fullPath.toAbsolutePath().toString(), fileName);
      if (!file.createNewFile()) {
        mediaRepository.delete(song);
        return new ResponseEntity<>(HttpStatus.CONFLICT);
      }
      mpFile.transferTo(file);

      // Add to playlist
      for (long playlistId : playlistIds) {
        if (playlistId > 1) { // Don't add to playlist with ID: 1
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

  @PostMapping("/import/")
  @Transactional
  public @ResponseBody ResponseEntity<Resource> importSongs(@RequestBody String data) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode dataNode = mapper.readTree(data);

    ArrayList<Media> newSongs = new ArrayList<>();
    HashMap<String, Artist> artistCache = new HashMap<>();
    HashMap<String, Album>  albumCache = new HashMap<>();
    for (JsonNode songNode : dataNode.get("songs")) {
      if(!this.isValidImportSong(songNode)){
        System.err.println("Error: Import is skipping invalid song.");
        continue;
      }

      JsonNode artistNode = songNode.get("artist");
      long artistId = 1; // 1 is Fallback
      if (artistNode.get("id") != null && artistNode.get("id").isNumber()) {
        artistId = artistNode.get("id").longValue();
      } else if (artistNode.get("name") != null && artistNode.get("name").isTextual()) {
        String artistName = artistNode.get("name").asText();
        if (artistCache.containsKey(artistName)) {
          artistId = artistCache.get(artistName).getId();
        } else {
          List<Artist> artistQueryResult = artistRepository.findByName(artistName);
          Artist artist = artistQueryResult.size() == 0
              ? artistRepository.save(new Artist(artistName))
              : artistQueryResult.get(0);
          artistId = artist.getId();
          artistCache.put(artistName, artist);
        }
      }

      JsonNode albumNode = songNode.get("album");
      long albumId = 1; // 1 is Fallback
      if (albumNode.get("id") != null && albumNode.get("id").isNumber()) {
        albumId = albumNode.get("id").longValue();
      } else if (albumNode.get("name") != null && albumNode.get("name").isTextual()) {
        String albumName = albumNode.get("name").asText();
        if (albumCache.containsKey(albumName)) {
          albumId = albumCache.get(albumName).getId();
        } else {
          List<Album> albumQueryResult = albumRepository.findByName(albumName);
          Album album = albumQueryResult.size() == 0
              ? albumRepository.save(new Album(albumName))
              : albumQueryResult.get(0);
          albumId = album.getId();
          albumCache.put(albumName, album);
        }
      }

      JsonNode genresNode = songNode.get("genres");
      List<Long> genreIds = new ArrayList<>();
      if (genresNode.isArray()) {
        for (JsonNode genreNode : genresNode) {
          genreIds.add(genreNode.get("id").asLong());
        }
      }
      JsonNode tagsNode = songNode.get("tags");
      List<Long> tagIds = new ArrayList<>();
      if (tagsNode.isArray()) {
        for (JsonNode tagNode : tagsNode) {
          tagIds.add(tagNode.get("id").asLong());
        }
      }

      Optional<Artist> artist = artistRepository.findById(artistId);
      Optional<Album> album = albumRepository.findById(albumId);
      Iterable<Genre> genres = genreRepository.findAllById(genreIds);
      Iterable<Tag> tags = tagRepository.findAllById(tagIds);


      // Create new song
      Song song = new Song(songNode.get("title").asText(), songNode.get("uri").asText());
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
      newSongs.add(mediaRepository.save(song));
    }

    List<Long> playlistIds = new ArrayList<>();
    if (dataNode.get("playlists").isArray()) {
      for (JsonNode jsonNode : dataNode.get("playlists")) {
        playlistIds.add(jsonNode.asLong());
      }
    }

    // Add to playlist
    for (long playlistId : playlistIds) {
      if (playlistId > 1) { // Don't add to playlist with ID: 1
        Optional<Playlist> playlist = playlistRepository.findById(playlistId);
        playlist.ifPresent(pl -> {
          if (pl.getSongs() == null) {
            pl.setSongs(new ArrayList<>());
          }
          pl.getSongs().addAll(newSongs);
          playlistRepository.save(pl);
        });
      }
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  private boolean isValidImportSong(JsonNode songNode) {
    return songNode.has("title")
        && songNode.has("artist")
        && songNode.has("album")
        && songNode.has("genres")
        && songNode.has("tags");
  }

  @GetMapping("/all")
  public @ResponseBody Page<Media> getSessions(
      @PathParam("sort") Optional<String> sort,
      @PathParam("order") Optional<String> order,
      @PathParam("page") Optional<Integer> page,
      @PathParam("pageSize") Optional<Integer> pageSize
  ) {
    Pageable pageable;
    if (page.isPresent() && pageSize.isPresent()) {
      if (sort.isPresent() && order.isPresent()) {
        if (order.get().equalsIgnoreCase("desc")) {
          pageable = PageRequest.of(page.get(), pageSize.get(), Sort.by(sort.get()).descending());
        } else {
          pageable = PageRequest.of(page.get(), pageSize.get(), Sort.by(sort.get()).ascending());
        }
      } else {
        pageable = PageRequest.of(page.get(), pageSize.get());
      }
    } else {
      pageable = PageRequest.of(0, Integer.MAX_VALUE);
    }
    Page<Media> result = mediaRepository.findAll(pageable);
    System.out.println(result.getContent().stream().map(Media::getTitle).toList());
    return result;
  }

  @GetMapping("/{id}")
  public @ResponseBody Optional<Media> getSong(@PathVariable Long id) {
    return mediaRepository.findById(id);
  }

  @GetMapping("/{id}/data/{offset}")
  @PreSignedUrlEnabled
  public @ResponseBody ResponseEntity<Resource> getSongData(@PathVariable String id, @PathVariable String offset) throws IOException, UnsupportedAudioFileException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException {
    Optional<Media> song = this.mediaRepository.findById(Long.parseLong(id));
    long offsetMillis = Long.parseLong(offset);
    if (song.isPresent()) {
      if (song.get().getType() == SONG && ((Song)song.get()).isSpotify()) {
        System.err.println("Error: Tried to access song data for a spotify resource.");
        return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
      }
      File origFile = new File(storagePath.toAbsolutePath() + song.get().getUri());
      File outFile;
      if (song.get().getUri().endsWith(".wav")) {
        outFile = cutWaveFile(origFile, offsetMillis);
      } else if (song.get().getUri().endsWith(".mp3")) {
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

  @GetMapping(path = "/getSongsByKeyword/{keyword}")
  public List<Media> getSongsByKeyword(@PathVariable String keyword) {
    List<Media> songs = mediaRepository.findByTitleContains(keyword);
    System.out.println(songs.size());
    System.out.println(keyword);
    return songs;
  }

  @GetMapping(path = "/getSongsByArtist/{keyword}")
  public List<Media> getSongsByArtist(@PathVariable String[] keyword) {
    List<Media> songs = mediaRepository.findByArtist(keyword);
    System.out.println(songs.size());
    System.out.println(Arrays.toString(keyword));
    return songs;
  }

  @GetMapping(path = "/getSongsByGenre/{keyword}")
  public List<Media> getSongsByGenre(@PathVariable String[] keyword) {
    List<Media> songs = mediaRepository.findByGenre(keyword);
    System.out.println(songs.size());
    System.out.println(Arrays.toString(keyword));
    return songs;
  }

  @PutMapping(path = "/deleteSongById/{songId}")
  public void deleteSongById(@PathVariable Long songId) {
    Media media = mediaRepository.findById(songId).orElseThrow(() -> new RuntimeException("Song not found"));
    media.getPlaylists().forEach(playlist ->
        playlist.setSongs(playlist.getSongs()
            .stream()
            .filter(song1 -> song1.getId() != media.getId())
            .collect(Collectors.toList()))
    );
    mediaRepository.delete(media);
  }

  @PutMapping(path = "/deleteSongs")
  public void deleteSongs(@RequestBody Media[] toBeDeletedMedia) {
    for (Media toBeDeleted : toBeDeletedMedia) {
      Media media = mediaRepository.findById(toBeDeleted.getId()).orElseThrow(() -> new RuntimeException("Song not found"));
      media.getPlaylists().forEach(playlist ->
          playlist.setSongs(playlist.getSongs()
              .stream()
              .filter(song1 -> song1.getId() != media.getId())
              .collect(Collectors.toList()))
      );
      mediaRepository.delete(media);
    }
  }

  @PutMapping(path = "/editSong")
  public ResponseEntity<String> editSong(@RequestBody Song alteredSong) {
    Optional<Song> song = this.songRepository.findById(alteredSong.getId());
    if (song.isPresent()) {
      Song originalSong = song.get();
      originalSong.setTitle(alteredSong.getTitle());
      originalSong.setArtist(alteredSong.getArtist());
      originalSong.setAlbum(alteredSong.getAlbum());
      originalSong.setGenres(alteredSong.getGenres());
      originalSong.setTags(alteredSong.getTags());
      System.out.println(originalSong.getTitle());
      System.out.println(originalSong.getArtist());
      System.out.println(originalSong.getGenres());
      mediaRepository.save(originalSong);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(HttpStatus.OK);
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
