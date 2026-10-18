package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.model.media.MinimalMedia;

import java.util.List;

public class StopCommand extends Command {
  /** Only set when the queue ended: the last media moved into the history, there is no current media. */
  List<MinimalMedia> queue;
  List<MinimalMedia> history;

  public StopCommand() {
    super("Stop");
  }

  public StopCommand(List<MinimalMedia> queue, List<MinimalMedia> history) {
    super("Stop");
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
