package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.song.MinimalSong;

import java.util.List;

public class UpdateQueueCommand extends UpdateCommand {

  List<MinimalSong> queue;

  public UpdateQueueCommand() {
    super("Queue");
  }

  public UpdateQueueCommand(List<MinimalSong> queue) {
    super("Queue");
    this.queue = queue;
  }

  public List<MinimalSong> getQueue() {
    return queue;
  }

  public void setQueue(List<MinimalSong> queue) {
    this.queue = queue;
  }
}
