package de.crow08.musicstreamserver.wscommunication.commands;

public class SkipCommand extends Command{
  String songId;
  long time;

  public SkipCommand(String songId, long time) {
    super("Skip");
    this.songId = songId;
    this.time = time;
  }

  public SkipCommand() {
    super("Skip");
  }

  public String getSongId() {
    return songId;
  }

  public void setSongId(String songId) {
    this.songId = songId;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
