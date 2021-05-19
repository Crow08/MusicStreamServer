package de.crow08.musicstreamserver.playlists;

import de.crow08.musicstreamserver.authentication.AuthenticatedUser;
import de.crow08.musicstreamserver.song.Song;
import de.crow08.musicstreamserver.users.User;
import de.crow08.musicstreamserver.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/playlists")
public class PlaylistResource {

  private final PlaylistRepository playlistRepository;
  private final UserRepository userRepository;

  @Autowired
  public PlaylistResource(PlaylistRepository playlistRepository, UserRepository userRepository) {
    this.playlistRepository = playlistRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<Playlist> getPlaylists() {
    return playlistRepository.findAll();
  }

  @GetMapping("/{id}")
  public @ResponseBody Optional<Playlist> getSong(@PathVariable String id) {
    return playlistRepository.findById(Long.parseLong(id));
  }

  @PutMapping(path = "/{playlistId}/addSongToPlaylist/{songId}")
  public ResponseEntity<Resource> addSongToPlaylist(@PathVariable Long songId, @PathVariable Long playlistId) {
    Optional<Playlist> playlist = playlistRepository.findById(playlistId);
    if (playlist.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    if (playlist.get().getSongs().stream().anyMatch(song -> song.getId() == songId)) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
    Song song = new Song(songId);
    playlist.get().getSongs().add(song);
    playlistRepository.save(playlist.get());
    return new ResponseEntity<>(HttpStatus.OK);
  }


  @PutMapping(path = "/{playlistId}/addSongsToPlaylist/")
  public ResponseEntity<String> addSongsToPlaylist(@RequestBody Long[] song_ids, @PathVariable Long playlistId) {
    Optional<Playlist> playlist = playlistRepository.findById(playlistId);
    if (playlist.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    ArrayList<Song> songsToAdd = new ArrayList<>();
    for (Long song_id : song_ids) {
      if (playlist.get().getSongs().stream().noneMatch(plSong -> song_id == plSong.getId())) {
        songsToAdd.add(new Song(song_id));
      }
    }
    if (songsToAdd.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
    playlist.get().getSongs().addAll(songsToAdd);
    playlistRepository.save(playlist.get());
    if (song_ids.length != songsToAdd.size()) {
      return ResponseEntity.ok().body(songsToAdd.size() + " of " + song_ids.length + " songs added");
    }
    return ResponseEntity.ok().body("Successfully added "+ song_ids.length + " songs");
  }

  @PostMapping(path = "/")
  public @ResponseBody long createNewPlaylist(@RequestBody String name) throws Exception {
    AuthenticatedUser authUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new Exception("User Not found!"));
    Playlist playlist = new Playlist(name, user);
    playlistRepository.save(playlist);
    return playlist.getId();
  }
}
