package de.crow08.musicstreamserver.wscommunication.commands;

public class SkipCommand extends Command{
  long songId;
  long time;

  public SkipCommand(long songId, long time) {
    super("Skip");
    this.songId = songId;
    this.time = time;
  }

  public SkipCommand() {
    super("Skip");
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
}
