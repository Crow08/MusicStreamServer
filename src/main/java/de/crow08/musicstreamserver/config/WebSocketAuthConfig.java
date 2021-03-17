package de.crow08.musicstreamserver.config;

import de.crow08.musicstreamserver.wscommunication.WebSocketSessionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

  private final WebSocketSessionController webSocketSessionController;

  @Autowired
  public WebSocketAuthConfig(final WebSocketSessionController webSocketSessionController) {
    this.webSocketSessionController = webSocketSessionController;
  }

  @Override
  public void configureClientInboundChannel(final ChannelRegistration registration) {
    registration.interceptors(new WebSocketAuthInterceptor(this.webSocketSessionController));
  }

  static class WebSocketAuthInterceptor implements ChannelInterceptor {
    private static final String USERNAME_HEADER = "login";
    private static final String PASSWORD_HEADER = "usercode";
    private static final String SESSION_HEADER = "session";
    private final WebSocketSessionController webSocketSessionController;

    public WebSocketAuthInterceptor(final WebSocketSessionController webSocketSessionController) {
      this.webSocketSessionController = webSocketSessionController;
    }

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
      final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

      if (accessor != null && StompCommand.CONNECT == accessor.getCommand()) {
        final String username = accessor.getFirstNativeHeader(USERNAME_HEADER);
        final String password = accessor.getFirstNativeHeader(PASSWORD_HEADER);
        final String musicSessionId = accessor.getFirstNativeHeader(SESSION_HEADER);
        String wsSessionId = (String) accessor.getHeader("simpSessionId");
        final UsernamePasswordAuthenticationToken user = webSocketSessionController.authenticateNewConnection(username, password, wsSessionId, musicSessionId);

        accessor.setUser(user);
      }
      return message;
    }
  }
}
