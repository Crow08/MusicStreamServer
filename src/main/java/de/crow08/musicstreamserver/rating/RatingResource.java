package de.crow08.musicstreamserver.rating;

import de.crow08.musicstreamserver.media.Media;
import de.crow08.musicstreamserver.media.MediaRepository;
import de.crow08.musicstreamserver.users.User;
import de.crow08.musicstreamserver.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ratings")
public class RatingResource {

  final RatingRepository ratingRepository;
  private final MediaRepository mediaRepository;

  @Autowired
  public RatingResource(RatingRepository ratingRepository, UserRepository userRepository, MediaRepository mediaRepository) {
    this.ratingRepository = ratingRepository;
    this.mediaRepository = mediaRepository;
  }

  @PutMapping(path = "/{songId}/addUserRating/{userRating}")
  public void addUserRating(@PathVariable Long songId, @PathVariable short userRating) throws Exception {
    User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    Media media = mediaRepository.findById(songId).orElseThrow(() -> new Exception("Unable to apply rating: Song with Id " + songId + " not found"));
    ratingRepository.save(new Rating(user, media, userRating));
  }

  @GetMapping(path = "getUserRating/{songId}")
  public short getUserRating(@PathVariable Long songId) throws Exception {
    User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    Media media = mediaRepository.findById(songId).orElseThrow(() -> new Exception("Unable to get rating of Song with Id " + songId));
    Optional<Rating> rating = ratingRepository.findById(new RatingId(user, media));
    return rating.map(Rating::getRatingValue).orElse((short) 0);
  }

  @GetMapping(path = "/getSongRating/{songId}")
  public float getSongRating(@PathVariable Long songId) {
    List<Short> ratings = ratingRepository.findRatingsBySongId(songId);
    if (ratings.size() == 0) {
      return 0;
    }
    int ratingSum = ratings.stream().mapToInt(Short::intValue).sum();
    return ((float) ratingSum / ratings.size());
  }
}
