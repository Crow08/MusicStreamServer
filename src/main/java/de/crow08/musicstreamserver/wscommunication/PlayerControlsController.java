package de.crow08.musicstreamserver.wscommunication;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;

import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class PlayerControlsController {
  @MessageMapping("/sessions/{sessionId}/commands/play")
  @SendTo("/topic/sessions/{sessionId}")
  public String play(@DestinationVariable String sessionId, String message) {
    System.out.println("Recieved: " + sessionId + " - " + message);
    return "Play";
  }

  @MessageMapping("/sessions/{sessionId}/commands/pause")
  @SendTo("/topic/sessions/{sessionId}")
  public String pause(@DestinationVariable String sessionId, String message) {
    System.out.println("Recieved: " + sessionId + " - " + message);
    return "Pause";
  }

  @MessageMapping("/sessions/{sessionId}/commands/stop")
  @SendTo("/topic/sessions/{sessionId}")
  public String stop(@DestinationVariable String sessionId, String message) {
    System.out.println("Recieved: " + sessionId + " - " + message);
    return "Stop";
  }

  @MessageMapping("/sessions/{sessionId}/commands/skip")
  @SendTo("/topic/sessions/{sessionId}")
  public String skip(@DestinationVariable String sessionId, String message) {
    System.out.println("Recieved: " + sessionId + " - " + message);
    return "Skip";
  }
}
