package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.model.media.MinimalMedia;
import de.crow08.musicstreamserver.model.users.User;
import de.crow08.musicstreamserver.sessions.Session;

import java.util.List;

public class JoinCommand extends Command {

  long userId;
  MinimalMedia currentMedia;
  List<MinimalMedia> queue;
  List<MinimalMedia> history;
  List<User> sessionUsers;
  Session.SessionState sessionState;
  boolean loopMode;
  long startServerTime;
  long startMediaTime;

  boolean isVideo;

  public JoinCommand() {
    super("Join");
  }

  public JoinCommand(long userId, MinimalMedia currentMedia, List<MinimalMedia> queue, List<MinimalMedia> history,
                     Session.SessionState sessionState, boolean loopMode, long startServerTime, long startMediaTime, List<User> sessionUsers) {
    super("Join");
    this.userId = userId;
    this.currentMedia = currentMedia;
    this.queue = queue;
    this.history = history;
    this.sessionState = sessionState;
    this.loopMode = loopMode;
    this.startServerTime = startServerTime;
    this.startMediaTime = startMediaTime;
    this.sessionUsers = sessionUsers;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public MinimalMedia getCurrentMedia() {
    return currentMedia;
  }

  public void setCurrentMedia(MinimalMedia currentMedia) {
    this.currentMedia = currentMedia;
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

  public long getStartServerTime() {
    return startServerTime;
  }

  public void setStartServerTime(long startServerTime) {
    this.startServerTime = startServerTime;
  }

  public long getStartMediaTime() {
    return startMediaTime;
  }

  public void setStartMediaTime(long startMediaTime) {
    this.startMediaTime = startMediaTime;
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
