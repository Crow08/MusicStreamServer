package de.crow08.musicstreamserver.sessions;

import de.crow08.musicstreamserver.playlists.Playlist;
import de.crow08.musicstreamserver.queue.Queue;

import java.time.Duration;
import java.time.Instant;

public class Session {

  private long id;
  private String host;
  private String name;
  private SessionState sessionState = SessionState.STOP;
  private Instant songStarted = null;
  private Duration savedProgression = Duration.ZERO;
  private Queue queue = new Queue();
  private boolean loopMode;

  public Session() {
  }

  public Session(String host, String name) {
    this.host = host;
    this.name = name;
  }

  public Session(String host, String name, Playlist playlist) {
    this(host, name);
    this.queue = new Queue(playlist);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
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
