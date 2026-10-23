package de.crow08.musicstreamserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client")
public class ClientProperties {
  private String host;
  private int port;
  /** Origin of the client (scheme, host and optional port), e.g. behind a TLS proxy; defaults to http://host:port. */
  private String origin;
  private String storagePath;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  /** The only origin that may use the REST API (CORS) and the WebSocket. */
  public String getAllowedOrigin() {
    return origin != null && !origin.isBlank() ? origin : "http://" + host + ":" + port;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(String storagePath) {
    this.storagePath = storagePath;
  }
}
