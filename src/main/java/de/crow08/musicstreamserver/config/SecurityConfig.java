package de.crow08.musicstreamserver.config;

import de.crow08.musicstreamserver.model.media.Media;
import de.crow08.musicstreamserver.model.media.MediaRepository;
import de.crow08.musicstreamserver.model.media.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

  final MediaRepository mediaRepository;

  private final UserDetailsService userDetailsService;
  private final String clientStoragePath;
  private final ClientProperties clientProperties;

  @Autowired
  public SecurityConfig(MediaRepository mediaRepository, @Qualifier("authenticatedUserService") UserDetailsService userDetailsService, @Value("${client.storage-path}") String clientStoragePath, ClientProperties clientProperties) {
    this.mediaRepository = mediaRepository;
    this.userDetailsService = userDetailsService;
    this.clientStoragePath = clientStoragePath;
    this.clientProperties = clientProperties;
  }

  @Bean
  WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().antMatchers(HttpMethod.OPTIONS).and().ignoring().antMatchers("/ws/**");
  }

  @Autowired
  public void globalSecurityConfiguration(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors().and()
        .authorizeRequests(a -> a.antMatchers("/api/**").fullyAuthenticated())
        .httpBasic()
        .and()
        .csrf().disable();
    return http.build();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(clientProperties.getAllowedOrigin())
        .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD");
  }

  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    addMediaResourceHandler(registry, "/api/v1/media/data/**", media -> Optional.of(media.getUri()));
    // Thumbnail sprite for the seek bar preview, written next to the video by convert-video.ps1
    addMediaResourceHandler(registry, "/api/v1/media/thumbnails/**", media -> media.getType() == MediaType.VIDEO
        ? Optional.of(media.getUri().replaceFirst("\\.[^./\\\\]*$", "") + ".thumbs.jpg")
        : Optional.empty());
    // The client is served by this server as well (copy of its build in static/)
    registry
        .addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true)
        .addResolver(new ClientResourceResolver());
  }

  /**
   * Serves the files of the client. Paths that are no file belong to the router of the client (e.g. a reload on
   * /sessions/1/lobby) and get its index.html; unknown API and WebSocket paths and missing files are 404.
   */
  private static class ClientResourceResolver extends PathResourceResolver {
    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
      Resource resource = super.getResource(resourcePath, location);
      if (resource != null) {
        return resource;
      }
      boolean apiOrWebSocket = resourcePath.matches("(api|ws)(/.*)?");
      boolean file = resourcePath.substring(resourcePath.lastIndexOf('/') + 1).contains(".");
      return apiOrWebSocket || file ? null : super.getResource("index.html", location);
    }
  }

  /**
   * Serves a file belonging to the media whose id is the first path segment after the pattern. The file is resolved
   * against the classpath and the client storage path; a missing media or file results in 404.
   */
  private void addMediaResourceHandler(ResourceHandlerRegistry registry, String pathPattern,
                                       Function<Media, Optional<String>> fileOfMedia) {
    registry
        .addResourceHandler(pathPattern)
        .addResourceLocations("classpath:/","file:///"+clientStoragePath)
        .setCachePeriod(3600)
        .resourceChain(true)
        .addResolver(new MediaResourceResolver(fileOfMedia));
  }

  private class MediaResourceResolver implements ResourceResolver {
    private final Function<Media, Optional<String>> fileOfMedia;

    MediaResourceResolver(Function<Media, Optional<String>> fileOfMedia) {
      this.fileOfMedia = fileOfMedia;
    }

    @Override
    public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
      try {
        return getResource(requestPath, locations);
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
      try {
        return getResource(resourcePath, locations) != null ? resourcePath : null;
      } catch (IOException e) {
        return null;
      }
    }

    private Resource getResource(String requestPath, List<? extends Resource> locations) throws IOException {
      String trimmedPath = requestPath;
      trimmedPath = trimmedPath.contains("?") ? trimmedPath.substring(0, trimmedPath.indexOf("?")) : trimmedPath;
      trimmedPath = trimmedPath.contains("/") ? trimmedPath.substring(0, trimmedPath.indexOf("/")) : trimmedPath;

      Optional<String> file;
      try {
        file = mediaRepository.findById(Long.parseLong(trimmedPath)).flatMap(fileOfMedia);
      } catch (NumberFormatException e) {
        return null;
      }
      if (file.isPresent()) {
        for (Resource location : locations) {
          Resource resource = location.createRelative(file.get());
          if (resource.isReadable()) {
            return resource;
          }
        }
      }
      return null;
    }
  }

}
