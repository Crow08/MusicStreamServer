package de.crow08.musicstreamserver.wscommunication.commands;

public class StartCommand extends Command{
  String songId;
  long time;

  public StartCommand(String songId, long time) {
    super("Start");
    this.songId = songId;
    this.time = time;
  }

  public StartCommand() {
    super("Start");
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
