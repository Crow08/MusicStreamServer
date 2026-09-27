package de.crow08.musicstreamserver.config;

import de.crow08.musicstreamserver.sessions.Session;
import de.crow08.musicstreamserver.sessions.SessionRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Only lets members of a session send commands to it. Joining is the only command allowed for non-members.
 */
public class SessionMembershipInterceptor implements ChannelInterceptor {

  private static final Pattern SESSION_COMMAND = Pattern.compile("^/app/sessions/(\\d+)/commands/([^/]+)");

  private final SessionRepository sessionRepository;

  public SessionMembershipInterceptor(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  @Override
  public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
    final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null || accessor.getCommand() != StompCommand.SEND || accessor.getDestination() == null) {
      return message;
    }
    Matcher matcher = SESSION_COMMAND.matcher(accessor.getDestination());
    if (!matcher.find() || matcher.group(2).equals("join")) {
      return message;
    }
    Principal user = accessor.getUser();
    Optional<Session> session = sessionRepository.findById(Long.parseLong(matcher.group(1)));
    boolean member = user != null && session.isPresent() &&
        session.get().getUsers().stream().anyMatch(sessionUser -> sessionUser.getUsername().equals(user.getName()));
    if (!member) {
      System.out.println("Dropped command from non-member " + (user != null ? user.getName() : null) + ": " +
          accessor.getDestination());
      // returning null drops the message
      return null;
    }
    return message;
  }
}
