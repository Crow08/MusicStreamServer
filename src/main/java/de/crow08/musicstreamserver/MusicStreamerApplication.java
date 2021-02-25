package de.crow08.musicstreamserver;

import de.crow08.musicstreamserver.config.ClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ClientProperties.class)
public class MusicStreamerApplication {

  public static void main(String[] args) {
    SpringApplication.run(MusicStreamerApplication.class, args);
  }

}
