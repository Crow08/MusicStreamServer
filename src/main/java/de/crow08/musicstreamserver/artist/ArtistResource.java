package de.crow08.musicstreamserver.artist;

import de.crow08.musicstreamserver.authentication.AuthenticatedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/artist")
public class ArtistResource {

}
