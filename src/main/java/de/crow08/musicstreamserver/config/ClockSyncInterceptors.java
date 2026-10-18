package de.crow08.musicstreamserver.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.time.Instant;

/**
 * Passive clock synchronization of the clients. A client can tag a command with the STOMP header {@value #SYNC_ID}.
 * Every message the server sends while handling that command (usually the broadcast of the resulting command) carries
 * the tag back, together with the server times (epoch millis) at which the command was received
 * ({@value #SERVER_RECEIVED}) and the message was sent ({@value #SERVER_SENT}). With its own send and receive times
 * the client determines the round trip time and its clock offset like NTP, without extra ping messages.
 */
public final class ClockSyncInterceptors {

  static final String SYNC_ID = "sync-id";
  static final String SERVER_RECEIVED = "server-received";
  static final String SERVER_SENT = "server-sent";
  /** Internal header of the inbound message: the time the command was received. */
  private static final String RECEIVED_AT = ClockSyncInterceptors.class.getName() + ".receivedAt";

  /** Tag and receive time of the command handled by the current thread. */
  private static final ThreadLocal<Timing> CURRENT = new ThreadLocal<>();

  private ClockSyncInterceptors() {
  }

  /**
   * For the client inbound channel: records the receive time of tagged commands (on the receiving thread) and makes the
   * tag available to the responses (on the thread handling the command).
   */
  public static class Inbound implements ExecutorChannelInterceptor {

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
      StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
      if (accessor == null || accessor.getCommand() != StompCommand.SEND || accessor.getFirstNativeHeader(SYNC_ID) == null) {
        return message;
      }
      long now = Instant.now().toEpochMilli();
      if (accessor.isMutable()) {
        accessor.setHeader(RECEIVED_AT, now);
        return message;
      }
      StompHeaderAccessor copy = StompHeaderAccessor.wrap(message);
      copy.setHeader(RECEIVED_AT, now);
      return MessageBuilder.createMessage(message.getPayload(), copy.getMessageHeaders());
    }

    @Override
    public Message<?> beforeHandle(final Message<?> message, final MessageChannel channel, final MessageHandler handler) {
      StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
      Object receivedAt = message.getHeaders().get(RECEIVED_AT);
      if (accessor != null && receivedAt instanceof Long) {
        CURRENT.set(new Timing(accessor.getFirstNativeHeader(SYNC_ID), (Long) receivedAt));
      }
      return message;
    }

    @Override
    public void afterMessageHandled(final Message<?> message, final MessageChannel channel, final MessageHandler handler,
                                    final Exception ex) {
      CURRENT.remove();
    }
  }

  /**
   * For the broker channel: adds the tag and the server times to the messages sent while handling a tagged command.
   * Messages sent by other threads (e.g. the sync timeout) are not tagged.
   */
  public static class Outbound implements ChannelInterceptor {

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
      Timing timing = CURRENT.get();
      if (timing == null) {
        return message;
      }
      SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
      accessor.setNativeHeader(SYNC_ID, timing.syncId());
      accessor.setNativeHeader(SERVER_RECEIVED, String.valueOf(timing.receivedAt()));
      accessor.setNativeHeader(SERVER_SENT, String.valueOf(Instant.now().toEpochMilli()));
      return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }
  }

  private record Timing(String syncId, long receivedAt) {
  }
}
