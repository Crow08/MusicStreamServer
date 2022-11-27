package de.crow08.musicstreamserver.wscommunication.commands;

public class JumpCommand extends Command {
  long startServerTime;
  long startMediaTime;

  public JumpCommand() {
    super("Jump");
  }

  public JumpCommand(long startServerTime, long startOffset) {
    super("Jump");
    this.startServerTime = startServerTime;
    this.startMediaTime = startOffset;
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
}
