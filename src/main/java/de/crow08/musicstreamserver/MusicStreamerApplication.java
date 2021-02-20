package de.crow08.musicstreamserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ClientConfiguration.class)
public class MusicStreamerApplication {

  public static void main(String[] args) {
    SpringApplication.run(MusicStreamerApplication.class, args);
  }

}
