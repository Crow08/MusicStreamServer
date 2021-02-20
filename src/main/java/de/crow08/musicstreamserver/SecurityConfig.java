package de.crow08.musicstreamserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

  private final UserDetailsService userDetailsService;

  @Autowired
  public SecurityConfig(@Qualifier("authenticatedUserService") UserDetailsService userDetailsService) {
    super(true);
    this.userDetailsService = userDetailsService;
  }

  @Override
  public void configure(final WebSecurity web) {
    web.ignoring().antMatchers(HttpMethod.OPTIONS);
  }

  @Autowired
  public void globalSecurityConfiguration(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
  }

  @Override
  public void configure(final HttpSecurity http) throws Exception {
    http.cors().and()
        .authorizeRequests()
        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .anyRequest().fullyAuthenticated()
        .and()
        .httpBasic()
        .and()
        .csrf().disable();
  }

  @Value("${client.host}")
  private String host;

  @Value("${client.port}")
  private int port;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("http://" + host + ":" + port)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD");
  }

}
