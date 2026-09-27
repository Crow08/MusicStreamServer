package de.crow08.musicstreamserver.sessions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.crow08.musicstreamserver.model.playlists.Playlist;
import de.crow08.musicstreamserver.model.queue.Queue;
import de.crow08.musicstreamserver.model.users.User;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Session {

  /**
   * Guards all state of this session (playback state, timing, queue, users). Commands of a session are handled
   * concurrently by the WebSocket message handlers.
   */
  private final Object lock = new Object();
  private final SessionUserSynchronization sessionUserSynchronization = new SessionUserSynchronization();
  private long id;
  private List<User> users;
  private String name;
  private SessionState sessionState = SessionState.STOP;
  private Instant mediaStarted = null;
  private Duration savedProgression = Duration.ZERO;
  private Queue queue = new Queue();
  private boolean loopMode;

  @Deprecated
  public Session() {
  }

  public Session(String name) {
    // read and modified by concurrently handled WebSocket messages
    this.users = new CopyOnWriteArrayList<>();
    this.name = name;
  }

  public Session(String name, Playlist playlist) {
    this(name);
    this.queue = new Queue(playlist);
  }

  @JsonIgnore
  public Object getLock() {
    return lock;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  public SessionUserSynchronization getSessionUserSynchronization() {
    return sessionUserSynchronization;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Queue getQueue() {
    return queue;
  }

  public void setQueue(Queue queue) {
    this.queue = queue;
  }

  public SessionState getSessionState() {
    return sessionState;
  }

  public void setSessionState(SessionState sessionState) {
    this.sessionState = sessionState;
  }

  public Instant getMediaStarted() {
    return mediaStarted;
  }

  public void setMediaStarted(Instant mediaStarted) {
    this.mediaStarted = mediaStarted;
  }

  public Duration getSavedProgression() {
    return savedProgression;
  }

  public void setSavedProgression(Duration savedProgression) {
    this.savedProgression = savedProgression;
  }

  public boolean isLoopMode() {
    return loopMode;
  }

  public void setLoopMode(boolean loopMode) {
    this.loopMode = loopMode;
  }

  public enum SessionState {
    PLAY, STOP, SYNC_PLAY, SYNC_STOP, SYNC_PAUSE, PAUSE
  }
}
