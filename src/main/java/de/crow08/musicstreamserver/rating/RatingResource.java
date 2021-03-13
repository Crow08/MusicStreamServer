package de.crow08.musicstreamserver.rating;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.crow08.musicstreamserver.song.Song;
import de.crow08.musicstreamserver.song.SongRepository;
import de.crow08.musicstreamserver.users.User;
import de.crow08.musicstreamserver.users.UserRepository;

@RestController
@RequestMapping("/ratings")
public class RatingResource {

  final RatingRepository ratingRepository;
  private final SongRepository songRepository;

  @Autowired
  public RatingResource(RatingRepository ratingRepository, UserRepository userRepository, SongRepository songRepository) {
    this.ratingRepository = ratingRepository;
    this.songRepository = songRepository;
  }
  
  @PutMapping(path = "/{songId}/addUserRating/{userRating}")
  public void addUserRating(@PathVariable Long songId, @PathVariable short userRating) throws Exception {
    User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    Song song = songRepository.findById(songId).orElseThrow(() ->new Exception("Unable to apply rating: Song with Id " + songId + " not found"));
    ratingRepository.save(new Rating(user, song, userRating));
  }
  
  @GetMapping(path = "getUserRating/{songId}")
  public short getUserRating(@PathVariable Long songId) throws Exception {
    User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    Song song = songRepository.findById(songId).orElseThrow(() ->new Exception("Unable to get rating of Song with Id " + songId));
    Optional<Rating> rating = ratingRepository.findById(new RatingId(user, song));
    if (rating.isPresent()){
      return rating.get().getRatingValue();
    }else {
      return 0;
    }
  }
  
  @GetMapping(path = "/getSongRating/{songId}")
  public float getSongRating(@PathVariable Long songId) {
    List<Short> ratings = ratingRepository.findRatingsBySongId(songId);
    if (ratings.size() == 0) {
      return 0;
    }
    Integer ratingSum = ratings.stream().mapToInt(Short::intValue).sum();
    return ((float)ratingSum/ratings.size());
  }
}
