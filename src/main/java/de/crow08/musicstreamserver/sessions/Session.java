package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.queue.Queue;
import de.crow08.musicstreamserver.users.User;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Session {

  private long id;
  private List<User> users;
  private String name;
  private SessionState sessionState = SessionState.STOP;
  private Instant songStarted = null;
  private Duration savedProgression = Duration.ZERO;
  private Queue queue = new Queue();
  private boolean loopMode;

  @Deprecated
  public Session() {
  }

  public Session(String name) {
    this.users = new ArrayList<>();
    this.name = name;
  }

  public Session(String name, Playlist playlist) {
    this(name);
    this.queue = new Queue(playlist);
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

  public Instant getSongStarted() {
    return songStarted;
  }

  public void setSongStarted(Instant songStarted) {
    this.songStarted = songStarted;
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
    PLAY, STOP, PAUSE
  }
}
