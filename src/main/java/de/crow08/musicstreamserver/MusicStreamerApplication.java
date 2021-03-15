package de.crow08.musicstreamserver;

import de.crow08.musicstreamserver.config.ClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableConfigurationProperties(ClientProperties.class)
public class MusicStreamerApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(MusicStreamerApplication.class, args);
  }

}
