package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.sessions.Session;

import java.util.List;

public class JoinCommand extends Command {

  long songId;
  long time;
  long startOffset;
  Session.SessionState sessionState;
  long userId;
  boolean loop;
  List<String> queue;

  public JoinCommand() {
    super("Join");
  }

  public JoinCommand(long songId, long time, long startOffset, Session.SessionState sessionState, long userId, boolean loop, List<String> queue) {
    super("Join");
    this.songId = songId;
    this.time = time;
    this.startOffset = startOffset;
    this.sessionState = sessionState;
    this.userId = userId;
    this.loop = loop;
    this.queue = queue;
  }

  public long getSongId() {
    return songId;
  }

  public void setSongId(long songId) {
    this.songId = songId;
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

  public Session.SessionState getSessionState() {
    return sessionState;
  }

  public void setSessionState(Session.SessionState sessionState) {
    this.sessionState = sessionState;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public boolean isLoop() {
    return loop;
  }

  public void setLoop(boolean loop) {
    this.loop = loop;
  }

  public List<String> getQueue() {
    return queue;
  }

  public void setQueue(List<String> queue) {
    this.queue = queue;
  }
}
