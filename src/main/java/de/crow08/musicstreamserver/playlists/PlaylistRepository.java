package de.crow08.musicstreamserver.playlists;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import de.crow08.musicstreamserver.song.Song;

public interface PlaylistRepository extends CrudRepository<Playlist, Long> {
  
  @Query(nativeQuery = true, value = "INSERT INTO song_playlist (playlist_id, song_id) VALUES (:playlist_id, :song_id);")
  void addSongToPlaylist(String playlist_id, String song_id);
}
