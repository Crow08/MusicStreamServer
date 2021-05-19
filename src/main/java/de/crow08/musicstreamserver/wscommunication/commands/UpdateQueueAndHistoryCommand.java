package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.song.MinimalSong;

import java.util.List;

public class UpdateQueueAndHistoryCommand extends UpdateCommand {

  List<MinimalSong> queue;
  List<MinimalSong> history;

  public UpdateQueueAndHistoryCommand() {
    super("QueueAndHistory");
  }

  public UpdateQueueAndHistoryCommand(List<MinimalSong> queue, List<MinimalSong> history) {
    super("QueueAndHistory");
    this.queue = queue;
    this.history = history;
  }

  public List<MinimalSong> getQueue() {
    return queue;
  }

  public void setQueue(List<MinimalSong> queue) {
    this.queue = queue;
  }

  public List<MinimalSong> getHistory() {
    return history;
  }

  public void setHistory(List<MinimalSong> history) {
    this.history = history;
  }
}
