package de.crow08.musicstreamserver.wscommunication.commands;

public class JumpCommand extends Command {
  long startMediaTime;

  public JumpCommand() {
    super("Jump");
  }

  public JumpCommand(long startOffset) {
    super("Jump");
    this.startMediaTime = startOffset;
  }

  public long getStartMediaTime() {
    return startMediaTime;
  }

  public void setStartMediaTime(long startMediaTime) {
    this.startMediaTime = startMediaTime;
  }
}
