package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.media.MinimalMedia;

import java.util.List;

public class UpdateQueueAndHistoryCommand extends UpdateCommand {

  List<MinimalMedia> queue;
  List<MinimalMedia> history;

  public UpdateQueueAndHistoryCommand() {
    super("QueueAndHistory");
  }

  public UpdateQueueAndHistoryCommand(List<MinimalMedia> queue, List<MinimalMedia> history) {
    super("QueueAndHistory");
    this.queue = queue;
    this.history = history;
  }

  public List<MinimalMedia> getQueue() {
    return queue;
  }

  public void setQueue(List<MinimalMedia> queue) {
    this.queue = queue;
  }

  public List<MinimalMedia> getHistory() {
    return history;
  }

  public void setHistory(List<MinimalMedia> history) {
    this.history = history;
  }
}
