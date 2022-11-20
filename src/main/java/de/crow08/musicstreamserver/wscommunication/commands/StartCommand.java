package de.crow08.musicstreamserver.wscommunication.commands;

public class StartCommand extends Command {
  long songId;
  long time;
  boolean isVideo;

  public StartCommand(long songId, long time, boolean isVideo) {
    super("Start");
    this.songId = songId;
    this.time = time;
    this.isVideo = isVideo;
  }

  public StartCommand() {
    super("Start");
  }

  public long getSongId() {
    return songId;
  }

  public void setSongId(long songId) {
    this.songId = songId;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public boolean getIsVideo() {
    return isVideo;
  }

  public void setIsVideo(boolean isVideo) {
    this.isVideo = isVideo;
  }
}
