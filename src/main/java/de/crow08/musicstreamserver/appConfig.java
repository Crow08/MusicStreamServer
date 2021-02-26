package de.crow08.musicstreamserver;

import de.crow08.musicstreamserver.sessions.SessionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class appConfig {

  @Bean
  public SessionRepository getSessionRepository(){
    return new SessionRepository();
  }
}
