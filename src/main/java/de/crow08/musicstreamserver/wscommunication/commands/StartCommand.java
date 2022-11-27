package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.model.media.MinimalMedia;

public class StartCommand extends Command {
  MinimalMedia currentMedia;
  long startServerTime;

  public StartCommand(MinimalMedia currentMedia, long startServerTime) {
    super("Start");
    this.currentMedia = currentMedia;
    this.startServerTime = startServerTime;
  }

  public StartCommand() {
    super("Start");
  }

  public MinimalMedia getCurrentMedia() {
    return currentMedia;
  }

  public void setCurrentMedia(MinimalMedia currentMedia) {
    this.currentMedia = currentMedia;
  }

  public long getStartServerTime() {
    return startServerTime;
  }

  public void setStartServerTime(long startServerTime) {
    this.startServerTime = startServerTime;
  }
}
