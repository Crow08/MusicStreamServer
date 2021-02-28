package de.crow08.musicstreamserver.wscommunication.commands;

public class StartCommand extends Command{
  long songId;
  long time;
  long startOffset;

  public StartCommand(long songId, long time) {
    super("Start");
    this.songId = songId;
    this.time = time;
    this.startOffset = 0;
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

  public long getStartOffset() {
    return startOffset;
  }

  public void setStartOffset(long startOffset) {
    this.startOffset = startOffset;
  }
}
