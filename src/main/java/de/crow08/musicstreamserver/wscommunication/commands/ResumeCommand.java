package de.crow08.musicstreamserver.wscommunication.commands;

public class ResumeCommand extends Command {
  long startServerTime;

  public ResumeCommand(long startServerTime) {
    super("Resume");
    this.startServerTime = startServerTime;
  }

  public ResumeCommand() {
    super("Resume");
  }

  public long getStartServerTime() {
    return startServerTime;
  }

  public void setStartServerTime(long startServerTime) {
    this.startServerTime = startServerTime;
  }
}
