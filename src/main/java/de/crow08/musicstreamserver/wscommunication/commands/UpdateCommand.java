package de.crow08.musicstreamserver.wscommunication.commands;

public class UpdateCommand extends Command {
  String updateType;

  public UpdateCommand(String updateType) {
    super("Update");
    this.updateType = updateType;
  }

  public String getUpdateType() {
    return updateType;
  }

  public void setUpdateType(String updateType) {
    this.updateType = updateType;
  }
}
