package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.song.MinimalSong;

import java.util.List;

public class UpdateHistoryCommand extends UpdateCommand {

  List<MinimalSong> history;

  public UpdateHistoryCommand() {
    super("History");
  }

  public UpdateHistoryCommand(List<MinimalSong> history) {
    super("History");
    this.history = history;
  }

  public List<MinimalSong> getHistory() {
    return history;
  }

  public void setHistory(List<MinimalSong> history) {
    this.history = history;
  }
}
