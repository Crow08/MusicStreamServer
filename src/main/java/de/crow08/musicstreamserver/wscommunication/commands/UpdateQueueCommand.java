package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.model.media.MinimalMedia;

import java.util.List;

public class UpdateQueueCommand extends UpdateCommand {

  List<MinimalMedia> queue;

  public UpdateQueueCommand() {
    super("Queue");
  }

  public UpdateQueueCommand(List<MinimalMedia> queue) {
    super("Queue");
    this.queue = queue;
  }

  public List<MinimalMedia> getQueue() {
    return queue;
  }

  public void setQueue(List<MinimalMedia> queue) {
    this.queue = queue;
  }
}
