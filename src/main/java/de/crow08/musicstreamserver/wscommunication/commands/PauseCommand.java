package de.crow08.musicstreamserver.wscommunication.commands;

public class PauseCommand extends Command {
  long mediaStopTime;

  public PauseCommand(long mediaStopTime) {
    super("Pause");
    this.mediaStopTime = mediaStopTime;
  }

  public PauseCommand() {
    super("Pause");
  }

  public long getMediaStopTime() {
    return mediaStopTime;
  }

  public void setMediaStopTime(long mediaStopTime) {
    this.mediaStopTime = mediaStopTime;
  }
}
