package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.sessions.Session;
import de.crow08.musicstreamserver.model.media.MinimalMedia;
import de.crow08.musicstreamserver.model.users.User;

import java.util.List;

public class JoinCommand extends Command {

  long userId;
  MinimalMedia currentSong;
  List<MinimalMedia> queue;
  List<MinimalMedia> history;
  List<User> sessionUsers;
  Session.SessionState sessionState;
  boolean loopMode;
  long time;
  long startOffset;

  boolean isVideo;

  public JoinCommand() {
    super("Join");
  }

  public JoinCommand(long userId, MinimalMedia currentSong, List<MinimalMedia> queue, List<MinimalMedia> history,
                     Session.SessionState sessionState, boolean loopMode, long time, long startOffset, boolean isVideo, List<User> sessionUsers) {
    super("Join");
    this.userId = userId;
    this.currentSong = currentSong;
    this.queue = queue;
    this.history = history;
    this.sessionState = sessionState;
    this.loopMode = loopMode;
    this.time = time;
    this.startOffset = startOffset;
    this.sessionUsers = sessionUsers;
    this.isVideo = isVideo;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public MinimalMedia getCurrentSong() {
    return currentSong;
  }

  public void setCurrentSong(MinimalMedia currentSong) {
    this.currentSong = currentSong;
  }

  public List<MinimalMedia> getQueue() {
    return queue;
  }

  public void setQueue(List<MinimalMedia> queue) {
    this.queue = queue;
  }

  public List<MinimalMedia> getHistory() {
    return history;
  }

  public void setHistory(List<MinimalMedia> history) {
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

  public boolean getIsVideo() {
    return isVideo;
  }

  public void setIsVideo(boolean isVideo) {
    this.isVideo = isVideo;
  }

  public List<User> getSessionUsers() {
    return sessionUsers;
  }

  public void setSessionUsers(List<User> sessionUsers) {
    this.sessionUsers = sessionUsers;
  }
}
