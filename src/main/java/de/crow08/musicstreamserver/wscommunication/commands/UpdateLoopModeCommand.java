package de.crow08.musicstreamserver.wscommunication.commands;

public class UpdateLoopModeCommand extends UpdateCommand {
  boolean loopMode;

  public UpdateLoopModeCommand() {
    super("LoopMode");
  }

  public UpdateLoopModeCommand(boolean loopMode) {
    super("LoopMode");
    this.loopMode = loopMode;
  }

  public boolean isLoopMode() {
    return loopMode;
  }

  public void setLoopMode(boolean loopMode) {
    this.loopMode = loopMode;
  }
}
