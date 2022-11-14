package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.model.media.MinimalMedia;

import java.util.List;

public class UpdateHistoryCommand extends UpdateCommand {

  List<MinimalMedia> history;

  public UpdateHistoryCommand() {
    super("History");
  }

  public UpdateHistoryCommand(List<MinimalMedia> history) {
    super("History");
    this.history = history;
  }

  public List<MinimalMedia> getHistory() {
    return history;
  }

  public void setHistory(List<MinimalMedia> history) {
    this.history = history;
  }
}
