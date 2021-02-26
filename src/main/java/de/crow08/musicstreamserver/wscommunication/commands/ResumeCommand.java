package de.crow08.musicstreamserver.wscommunication.commands;

public class ResumeCommand extends Command{
  long time;
  public ResumeCommand(long time) {
    super("Resume");
    this.time = time;
  }

  public ResumeCommand() {
    super("Resume");
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
