package de.crow08.musicstreamserver.users;

import de.crow08.musicstreamserver.authentication.AuthenticatedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserResource {

  @GetMapping("/login")
  public User getUser() {
    return this.getDTO((AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  private User getDTO(User user) {
    return new User(user.getId(), user.getUsername());
  }
}
