package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.sessions.UserStatus;

public class UpdateUserStatusCommand extends UpdateCommand {
  UserStatus userStatus;

  public UpdateUserStatusCommand() {
    super("UserStatus");
  }

  public UpdateUserStatusCommand(UserStatus userStatus) {
    super("UserStatus");
    this.userStatus = userStatus;
  }

  public UserStatus getUserStatus() {
    return userStatus;
  }

  public void setUserStatus(UserStatus userStatus) {
    this.userStatus = userStatus;
  }
}
