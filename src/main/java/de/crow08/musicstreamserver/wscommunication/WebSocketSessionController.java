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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionController {

  final UserRepository userRepository;
  private final List<DisconnectObserver> dcListeners = new ArrayList<>();
  private final List<ConnectObserver> cListeners = new ArrayList<>();
  private final Map<String, Connection> connections = new ConcurrentHashMap<>();

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

    User user = userRepository.findByUsername(username);
    if (user == null || !PasswordEncoderFactories.createDelegatingPasswordEncoder().matches(password, user.getPassword())) {
      throw new BadCredentialsException("Bad credentials for user " + username);
    }

    Connection connection = new Connection(user.getId());
    long musicSession = Long.parseLong(musicSessionId);
    if (musicSession != 0) {
      connection.musicSessions.add(musicSession);
    }
    this.connections.put(wsSessionId, connection);
    for (final ConnectObserver listener : this.cListeners) {
      listener.observeConnect(user.getId(), musicSession);
    }
    // null credentials, we do not pass the password along
    return new UsernamePasswordAuthenticationToken(
        username,
        null,
        Collections.singleton((GrantedAuthority) () -> "USER") // MUST provide at least one role
    );
  }

  /**
   * Records that the user of the given WebSocket connection joined a music session through this connection.
   */
  public void joinMusicSession(final String wsSessionId, final long musicSessionId) {
    Connection connection = this.connections.get(wsSessionId);
    if (connection != null) {
      connection.musicSessions.add(musicSessionId);
    }
  }

  /**
   * Records that the given WebSocket connection left a music session.
   *
   * @return true if the user has no other connection in that music session and should be removed from it.
   */
  public boolean leaveMusicSession(final String wsSessionId, final long userId, final long musicSessionId) {
    Connection connection = this.connections.get(wsSessionId);
    if (connection != null) {
      connection.musicSessions.remove(musicSessionId);
    }
    return !isUserConnectedToMusicSession(userId, musicSessionId);
  }

  /**
   * Handles a closed WebSocket connection. The user is only reported as disconnected from the music sessions no other
   * connection of the same user (e.g. another browser tab) is still part of.
   */
  public void disconnectSession(final String wsSessionId) {
    Connection connection = this.connections.remove(wsSessionId);
    if (connection == null) {
      return;
    }
    for (final long musicSessionId : connection.musicSessions) {
      if (!isUserConnectedToMusicSession(connection.userId, musicSessionId)) {
        for (final DisconnectObserver listener : this.dcListeners) {
          listener.observeDisconnect(connection.userId, musicSessionId);
        }
      }
    }
  }

  private boolean isUserConnectedToMusicSession(final long userId, final long musicSessionId) {
    return this.connections.values().stream()
        .anyMatch(c -> c.userId == userId && c.musicSessions.contains(musicSessionId));
  }

  public void addDisconnectListener(DisconnectObserver newListener) {
    dcListeners.add(newListener);
  }

  public void addConnectListener(ConnectObserver newListener) {
    cListeners.add(newListener);
  }

  public interface DisconnectObserver {
    void observeDisconnect(long userId, long sessionId);
  }

  public interface ConnectObserver {
    void observeConnect(long userId, long sessionId);
  }

  private static final class Connection {
    private final long userId;
    private final Set<Long> musicSessions = ConcurrentHashMap.newKeySet();

    private Connection(long userId) {
      this.userId = userId;
    }
  }
}
