package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.model.media.MinimalMedia;

import java.util.List;

public class SetMediaCommand extends Command {
  MinimalMedia currentMedia;
  List<MinimalMedia> queue;
  List<MinimalMedia> history;

  public SetMediaCommand(MinimalMedia currentMedia, List<MinimalMedia> queue, List<MinimalMedia> history) {
    super("SetMedia");
    this.currentMedia = currentMedia;
    this.queue = queue;
    this.history = history;
  }

  public SetMediaCommand() {
    super("Start");
  }

  public MinimalMedia getCurrentMedia() {
    return currentMedia;
  }

  public void setCurrentMedia(MinimalMedia currentMedia) {
    this.currentMedia = currentMedia;
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
