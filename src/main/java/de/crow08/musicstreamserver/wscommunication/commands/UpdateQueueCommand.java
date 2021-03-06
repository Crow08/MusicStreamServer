package de.crow08.musicstreamserver.wscommunication.commands;

import java.util.List;

public class UpdateQueueCommand extends UpdateCommand {

  List<String> queue;

  public UpdateQueueCommand() {
    super("Queue");
  }

  public UpdateQueueCommand(List<String> queue) {
    super("Queue");
    this.queue = queue;
  }

  public List<String> getQueue() {
    return queue;
  }

  public void setQueue(List<String> queue) {
    this.queue = queue;
  }
}
