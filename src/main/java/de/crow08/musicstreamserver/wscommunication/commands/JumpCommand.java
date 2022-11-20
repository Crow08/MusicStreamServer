package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.model.media.MinimalMedia;
import de.crow08.musicstreamserver.model.users.User;
import de.crow08.musicstreamserver.sessions.Session;

import java.util.List;

public class JumpCommand extends Command {
  long time;
  long startOffset;

  public JumpCommand() {
    super("Jump");
  }

  public JumpCommand(long time, long startOffset) {
    super("Jump");
    this.time = time;
    this.startOffset = startOffset;
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
