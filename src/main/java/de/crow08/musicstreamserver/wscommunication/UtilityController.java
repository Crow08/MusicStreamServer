package de.crow08.musicstreamserver.wscommunication;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
public class UtilityController {

  @MessageMapping("/util/latency")
  @SendTo("/topic/util/latency")
  public String play(String message) {
    System.out.println("Recieved: " + message);

    return String.valueOf(Instant.now().toEpochMilli());
  }
}
