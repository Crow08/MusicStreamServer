package de.crow08.musicstreamserver.spotify;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.AbstractAuthorizationRequest;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

@RestController
@RequestMapping("/spotify")
public class SpotifyResource {

  @Value("${spotify.client.id}")
  private String clientId;
  @Value("${spotify.client.secret}")
  private String clientSecret;
  @Value("${spotify.client.redirect}")
  private URI redirectUri;

  @PostMapping("/login")
  public @ResponseBody SpotifyToken getToken(@RequestBody String code) throws IOException, ParseException, SpotifyWebApiException {
    Function<SpotifyApi.Builder, AbstractAuthorizationRequest<AuthorizationCodeCredentials>> fn =
        b -> b.build().authorizationCode(code).build();
    AuthorizationCodeCredentials credentials = getCredentials(fn);
    return new SpotifyToken(credentials);
  }

  @PostMapping("/refresh")
  public @ResponseBody SpotifyToken refreshToken(@RequestBody String refreshToken) throws IOException, ParseException, SpotifyWebApiException {
    Function<SpotifyApi.Builder, AbstractAuthorizationRequest<AuthorizationCodeCredentials>> fn =
        b -> b.setRefreshToken(refreshToken).build().authorizationCodeRefresh().build();
    AuthorizationCodeCredentials credentials = getCredentials(fn);
    return new SpotifyToken(credentials);
  }

  private AuthorizationCodeCredentials getCredentials(Function<SpotifyApi.Builder, AbstractAuthorizationRequest<AuthorizationCodeCredentials>> fn) throws IOException, SpotifyWebApiException, ParseException {
    return fn.apply(SpotifyApi.builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRedirectUri(redirectUri))
        .execute();
  }

  private static class SpotifyToken {
    private String token;
    private String refreshToken;
    private Integer expiresIn;

    public SpotifyToken(AuthorizationCodeCredentials credentials) {
      this.token = credentials.getAccessToken();
      this.refreshToken = credentials.getRefreshToken();
      this.expiresIn = credentials.getExpiresIn();
    }

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }

    public String getRefreshToken() {
      return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
    }

    public Integer getExpiresIn() {
      return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
      this.expiresIn = expiresIn;
    }
  }
}
