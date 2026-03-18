package ai.concerto.event.auth.config;

import ai.concerto.event.auth.filter.PreApiTokenHeaderFilter;
import ai.concerto.event.auth.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@EnableWebSecurity
public class AuthTokenSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${auth.header.api-token}")
  private String apiTokenHeader;

  @Autowired
  private TokenService tokenService;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    PreApiTokenHeaderFilter apiTokenFilter = new PreApiTokenHeaderFilter(apiTokenHeader);
    apiTokenFilter.setAuthenticationManager(authentication -> {
      String token = (String) authentication.getPrincipal();
      if (!StringUtils.hasText(token) || !tokenService.validateApiToken(token)) {
        throw new BadCredentialsException("Invalid/missing api access token");
      }
      authentication.setAuthenticated(true);
      return authentication;
    });

    // add api token filter to /inbox/api/* endpoints
    http.antMatcher("/v0/**").csrf().disable().sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().addFilter(apiTokenFilter)
        .addFilterBefore(new ExceptionTranslationFilter(new Http403ForbiddenEntryPoint()),
            apiTokenFilter.getClass())
        .authorizeRequests().anyRequest().authenticated();

  }
}
