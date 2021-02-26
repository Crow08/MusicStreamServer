package de.crow08.musicstreamserver.wscommunication.commands;

public class PauseCommand extends Command{
  long position;

  public PauseCommand(long position) {
    super("Pause");
    this.position = position;
  }

  public PauseCommand() {
    super("Pause");
  }
}
