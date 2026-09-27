package de.crow08.musicstreamserver.wscommunication.commands;

/**
 * Sent only to the client whose command failed, so it can show the reason.
 */
public class ErrorCommand extends Command {

  String message;
  String destination;

  public ErrorCommand() {
    super("Error");
  }

  public ErrorCommand(String message, String destination) {
    super("Error");
    this.message = message;
    this.destination = destination;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }
}
