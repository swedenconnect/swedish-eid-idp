/*
 * Copyright 2023-2025 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.eid.idp.config;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.spring6.SpringTemplateEngine;
import se.swedenconnect.eid.idp.authn.SimulatedAuthenticationController;
import se.swedenconnect.eid.idp.authn.SimulatedAuthenticationProvider;
import se.swedenconnect.eid.idp.users.SimulatedUserDetailsManager;
import se.swedenconnect.eid.idp.users.UsersConfigurationProperties;
import se.swedenconnect.spring.saml.idp.config.configurers.Saml2IdpConfigurerAdapter;
import se.swedenconnect.spring.saml.idp.extensions.SignatureMessagePreprocessor;
import se.swedenconnect.spring.saml.idp.extensions.UserMessagePreprocessor;
import se.swedenconnect.spring.saml.idp.response.ThymeleafResponsePage;

import java.time.Duration;
import java.util.Objects;

/**
 * IdP configuration.
 *
 * @author Martin Lindström
 */
@Configuration
@EnableConfigurationProperties({ IdpConfigurationProperties.class, UsersConfigurationProperties.class })
public class IdpConfiguration {

  /** IdP configuration properties. */
  private final IdpConfigurationProperties properties;

  /** The simulated users. */
  private final UsersConfigurationProperties users;

  /** The context path. */
  @Setter
  @Value("${server.servlet.context-path:/}")
  private String contextPath;

  /**
   * Constructor.
   *
   * @param properties the configuration properties
   * @param users the user configuration
   */
  public IdpConfiguration(final IdpConfigurationProperties properties, final UsersConfigurationProperties users) {
    this.properties = Objects.requireNonNull(properties, "properties must not be null");
    this.users = Objects.requireNonNull(users, "users must not be null");
  }

  /**
   * For Tomcat configuration.
   *
   * @return a {@link TomcatAjpConfigurationProperties}
   */
  @Bean
  @ConfigurationProperties(prefix = "tomcat.ajp")
  TomcatAjpConfigurationProperties tomcatAjpConfigurationProperties() {
    return new TomcatAjpConfigurationProperties();
  }

  /**
   * Creates the {@link UserDetailsService} holding all simulated users.
   *
   * @return a {@link UserDetailsService}
   */
  @Bean
  SimulatedUserDetailsManager userDetailsService() {
    final SimulatedUserDetailsManager mgr = new SimulatedUserDetailsManager();
    this.users.getUsers().forEach(mgr::createUser);
    return mgr;
  }

  /**
   * Creates the {@link SimulatedAuthenticationProvider} which is the {@link AuthenticationProvider} that is responsible
   * for the user authentication.
   *
   * @return a {@link SimulatedAuthenticationProvider}
   */
  @Bean
  SimulatedAuthenticationProvider simulatedAuthenticationProvider() {
    final SimulatedAuthenticationProvider provider = new SimulatedAuthenticationProvider(
        this.properties.getAuthnPath(), this.properties.getResumePath(),
        this.properties.getSupportedLoas(), this.properties.getEntityCategories());
    provider.setName(this.properties.getProviderName());
    return provider;
  }

  /**
   * Gets a {@link Saml2IdpConfigurerAdapter} that applies custom configuration for the IdP.
   *
   * @param signMessageProcessor a {@link SignatureMessagePreprocessor} for display of sign messages
   * @param userMessageProcessor a {@link UserMessagePreprocessor} for display of user messages
   * @return a {@link Saml2IdpConfigurerAdapter}
   */
  @Bean
  Saml2IdpConfigurerAdapter samlIdpSettingsAdapter(final SignatureMessagePreprocessor signMessageProcessor,
      final UserMessagePreprocessor userMessageProcessor) {
    return (http, configurer) -> configurer.authnRequestProcessor(c -> c.authenticationProvider(
        pc -> {
          pc.signatureMessagePreprocessor(signMessageProcessor);
          pc.userMessagePreprocessor(userMessageProcessor);
        }));
  }

  /**
   * A response page using Thymeleaf to post the response.
   *
   * @param templateEngine the template engine
   * @return a {@link ThymeleafResponsePage}
   */
  @Bean
  ThymeleafResponsePage responsePage(final SpringTemplateEngine templateEngine) {
    return new ThymeleafResponsePage(templateEngine, "post-response.html");
  }

  /**
   * Creates a {@link CookieGenerator} for saving selected (simulated) user.
   *
   * @return a {@link CookieGenerator}
   */
  @Bean("selectedUserCookieGenerator")
  CookieGenerator selectedUserCookieGenerator() {
    return new CookieGenerator("selectedUser", this.contextPath, Duration.ofDays(365));
  }

  /**
   * Creates a {@link CookieGenerator} for saving custom users.
   *
   * @return a {@link CookieGenerator}
   */
  @Bean("savedUsersCookieGenerator")
  CookieGenerator savedUsersCookieGenerator() {
    return new CookieGenerator("savedUsers", this.contextPath, Duration.ofDays(365));
  }

  /**
   * Creates a {@link CookieGenerator} for saving custom users.
   *
   * @return a {@link CookieGenerator}
   */
  @Bean(SimulatedAuthenticationController.AUTO_AUTHN_COOKIE_NAME)
  CookieGenerator autoAuthnCookieGenerator() {
    return new CookieGenerator(SimulatedAuthenticationController.AUTO_AUTHN_COOKIE_NAME, this.contextPath,
        Duration.ofDays(365));
  }

  /**
   * Gets a default {@link SecurityFilterChain} protecting other resources.
   *
   * @param http the HttpSecurity object
   * @return a SecurityFilterChain
   * @throws Exception for config errors
   */
  @Bean
  @Order(2)
  SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http) throws Exception {

    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers(this.properties.getAuthnPath() + "/**").permitAll()
            .requestMatchers("/images/**", "/error", "/css/**", "/scripts/**", "/webjars/**").permitAll()
            .requestMatchers(SimulatedAuthenticationController.AUTO_AUTHN_PATH + "/**").permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
            .anyRequest().denyAll());

    return http.build();
  }

}
