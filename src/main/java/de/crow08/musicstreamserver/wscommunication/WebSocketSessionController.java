package de.crow08.musicstreamserver.wscommunication;

import de.crow08.musicstreamserver.model.users.User;
import de.crow08.musicstreamserver.model.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WebSocketSessionController {

  final UserRepository userRepository;
  private final List<DisconnectObserver> dcListeners = new ArrayList<>();
  private final List<ConnectObserver> cListeners = new ArrayList<>();
  private final Map<String, Long> sessions = new HashMap<>();

  @Autowired
  public WebSocketSessionController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // This method MUST return a UsernamePasswordAuthenticationToken instance, the spring security chain is testing it with 'instanceof' later on. So don't use a subclass of it or any other class
  public UsernamePasswordAuthenticationToken authenticateNewConnection(final String username, final String password, final String wsSessionId, final String musicSessionId) throws AuthenticationException {
    if (username == null || username.trim().isEmpty()) {
      throw new AuthenticationCredentialsNotFoundException("Username was null or empty.");
    }
    if (password == null || password.trim().isEmpty()) {
      throw new AuthenticationCredentialsNotFoundException("Password was null or empty.");
    }
    // Add your own logic for retrieving user in fetchUserFromDb()
    User user = userRepository.findByUsername(username);
    if (user == null || !PasswordEncoderFactories.createDelegatingPasswordEncoder().matches(password, user.getPassword())) {
      throw new BadCredentialsException("Bad credentials for user " + username);
    }

    this.sessions.put(wsSessionId, user.getId());
    for (final ConnectObserver listener : this.cListeners) {
      listener.observeConnect(user.getId(), Long.parseLong(musicSessionId));
    }
    // null credentials, we do not pass the password along
    return new UsernamePasswordAuthenticationToken(
        username,
        null,
        Collections.singleton((GrantedAuthority) () -> "USER") // MUST provide at least one role
    );
  }

  public void disconnectSession(final String sessionId) {
    Long userId = this.sessions.remove(sessionId);
    if (userId != null) {
      for (final DisconnectObserver listener : this.dcListeners) {
        listener.observeDisconnect(userId);
      }
    }
  }

  public void addDisconnectListener(DisconnectObserver newListener) {
    dcListeners.add(newListener);
  }

  public void addConnectListener(ConnectObserver newListener) {
    cListeners.add(newListener);
  }

  public interface DisconnectObserver {
    void observeDisconnect(Long UserId);
  }

  public interface ConnectObserver {
    void observeConnect(long userId, long sessionId);
  }
}

