package de.crow08.musicstreamserver.wscommunication.commands;

import de.crow08.musicstreamserver.model.media.MinimalMedia;

public class SetMediaCommand extends Command {
  MinimalMedia currentMedia;

  public SetMediaCommand(MinimalMedia currentMedia) {
    super("SetMedia");
    this.currentMedia = currentMedia;
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
}
