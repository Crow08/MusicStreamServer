package de.crow08.musicstreamserver.wscommunication.commands;

public class LeaveCommand extends Command {

  long userId;

  public LeaveCommand() {
    super("Leave");
  }

  public LeaveCommand(long userId) {
    super("Leave");
    this.userId = userId;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }
}
