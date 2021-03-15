package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.sessions.Session;
import de.crow08.musicstreamserver.song.MinimalSong;

import java.util.List;

public class JoinCommand extends Command {

  long userId;
  MinimalSong currentSong;
  List<MinimalSong> queue;
  List<MinimalSong> history;
  Session.SessionState sessionState;
  boolean loopMode;
  long time;
  long startOffset;

  public JoinCommand() {
    super("Join");
  }

  public JoinCommand(long userId, MinimalSong currentSong, List<MinimalSong> queue, List<MinimalSong> history,
                     Session.SessionState sessionState, boolean loopMode, long time, long startOffset) {
    super("Join");
    this.userId = userId;
    this.currentSong = currentSong;
    this.queue = queue;
    this.history = history;
    this.sessionState = sessionState;
    this.loopMode = loopMode;
    this.time = time;
    this.startOffset = startOffset;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public MinimalSong getCurrentSong() {
    return currentSong;
  }

  public void setCurrentSong(MinimalSong currentSong) {
    this.currentSong = currentSong;
  }

  public List<MinimalSong> getQueue() {
    return queue;
  }

  public void setQueue(List<MinimalSong> queue) {
    this.queue = queue;
  }

  public List<MinimalSong> getHistory() {
    return history;
  }

  public void setHistory(List<MinimalSong> history) {
    this.history = history;
  }

  public Session.SessionState getSessionState() {
    return sessionState;
  }

  public void setSessionState(Session.SessionState sessionState) {
    this.sessionState = sessionState;
  }

  public boolean isLoopMode() {
    return loopMode;
  }

  public void setLoopMode(boolean loopMode) {
    this.loopMode = loopMode;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public long getStartOffset() {
    return startOffset;
  }

  public void setStartOffset(long startOffset) {
    this.startOffset = startOffset;
  }
}
