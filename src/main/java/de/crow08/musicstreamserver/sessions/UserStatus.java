package de.crow08.musicstreamserver.sessions;

/**
 * Playback status of a session user, as reported by its client and shown to the other users of the session.
 * Only kept in memory, like the session itself.
 */
public class UserStatus {

  /** Upper bound for the reported values (ms), larger values are clamped. */
  private static final int MAX_REPORTED_MILLIS = 24 * 60 * 60 * 1000;

  private long userId;
  private State state = State.IDLE;
  /** Average round trip time to the server in ms, null if not measured yet. */
  private Integer latency;
  /** Expected minus actual playback position in ms (positive: behind the session), null if not playing. */
  private Integer drift;

  public UserStatus() {
  }

  public UserStatus(long userId, State state, Integer latency, Integer drift) {
    this.userId = userId;
    this.state = state;
    this.latency = latency;
    this.drift = drift;
  }

  /**
   * Creates the status of the given user from the values reported by its client. The user id is not taken from the
   * report, values out of range are clamped.
   */
  public static UserStatus fromReport(long userId, UserStatus report) {
    State state = report.state != null ? report.state : State.IDLE;
    Integer latency = report.latency != null ? clamp(report.latency, 0) : null;
    Integer drift = report.drift != null ? clamp(report.drift, -MAX_REPORTED_MILLIS) : null;
    return new UserStatus(userId, state, latency, drift);
  }

  private static int clamp(int value, int min) {
    return Math.max(min, Math.min(MAX_REPORTED_MILLIS, value));
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public Integer getLatency() {
    return latency;
  }

  public void setLatency(Integer latency) {
    this.latency = latency;
  }

  public Integer getDrift() {
    return drift;
  }

  public void setDrift(Integer drift) {
    this.drift = drift;
  }

  public enum State {
    /** No media loaded, e.g. while the session is stopped. */
    IDLE,
    /** The media is loading or buffering: the client can not play right now. */
    LOADING,
    /** The media is loaded and can be played. */
    READY,
    /** The media can not be played by this client, e.g. a Spotify song without a connected Premium account. */
    UNAVAILABLE
  }
}
