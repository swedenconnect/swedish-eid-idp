/*
 * Copyright 2023 Sweden Connect
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

import java.time.Duration;
import java.util.Objects;

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EncryptionMethod;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.credential.UsageType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.util.CookieGenerator;
import org.thymeleaf.spring5.SpringTemplateEngine;

import se.swedenconnect.eid.idp.authn.SimulatedAuthenticationProvider;
import se.swedenconnect.eid.idp.users.SimulatedUserDetailsManager;
import se.swedenconnect.eid.idp.users.UsersConfigurationProperties;
import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.sweid.saml2.authn.psc.RequestedPrincipalSelection;
import se.swedenconnect.opensaml.sweid.saml2.authn.psc.build.MatchValueBuilder;
import se.swedenconnect.opensaml.sweid.saml2.authn.psc.build.RequestedPrincipalSelectionBuilder;
import se.swedenconnect.spring.saml.idp.config.annotation.web.configurers.Saml2IdpConfigurerAdapter;
import se.swedenconnect.spring.saml.idp.response.ThymeleafResponsePage;

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
    this.users.getUsers().stream().forEach(u -> mgr.createUser(u));
    return mgr;
  }

  /**
   * Creates the {@link SimulatedAuthenticationProvider} which is the {@link AuthenticationProvider} that is responsible
   * of the user authentication.
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

  @Bean
  Saml2IdpConfigurerAdapter samlIdpSettingsAdapter() {
    return (http, configurer) -> {
      configurer
//          .responseSender(s -> s.setResponsePage("/idp/post"))
          .idpMetadataEndpoint(mdCustomizer -> {
            mdCustomizer.entityDescriptorCustomizer(this.metadataCustomizer());
          });
    };
  }

  private Customizer<EntityDescriptor> metadataCustomizer() {
    return e -> {
      final RequestedPrincipalSelection rps = RequestedPrincipalSelectionBuilder.builder()
          .matchValues(MatchValueBuilder.builder()
              .name(AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER)
              .build())
          .build();

      final IDPSSODescriptor ssoDescriptor = e.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
      Extensions extensions = ssoDescriptor.getExtensions();
      if (extensions == null) {
        extensions = (Extensions) XMLObjectSupport.buildXMLObject(Extensions.DEFAULT_ELEMENT_NAME);
        ssoDescriptor.setExtensions(extensions);
      }
      extensions.getUnknownXMLObjects().add(rps);

      KeyDescriptor encryption = null;
      for (final KeyDescriptor kd : ssoDescriptor.getKeyDescriptors()) {
        if (Objects.equals(UsageType.ENCRYPTION, kd.getUse())) {
          encryption = kd;
          break;
        }
        if (kd.getUse() == null || Objects.equals(UsageType.UNSPECIFIED, kd.getUse())) {
          encryption = kd;
        }
      }
      if (encryption != null) {
        final String[] algs = { "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p",
            "http://www.w3.org/2009/xmlenc11#aes256-gcm",
            "http://www.w3.org/2009/xmlenc11#aes192-gcm",
            "http://www.w3.org/2009/xmlenc11#aes128-gcm"
        };
        for (final String alg : algs) {
          final EncryptionMethod method =
              (EncryptionMethod) XMLObjectSupport.buildXMLObject(EncryptionMethod.DEFAULT_ELEMENT_NAME);
          method.setAlgorithm(alg);
          encryption.getEncryptionMethods().add(method);
        }
      }

    };
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
    final CookieGenerator c = new CookieGenerator();
    c.setCookieName("selectedUser");
    c.setCookieHttpOnly(true);
    c.setCookieSecure(true);
    c.setCookieMaxAge((int) Duration.ofDays(365).getSeconds());
    return c;
  }

  
  /**
   * Creates a {@link CookieGenerator} for saving custom users.
   * 
   * @return a {@link CookieGenerator}
   */
  @Bean("savedUsersCookieGenerator")
  CookieGenerator savedUsersCookieGenerator() {
    final CookieGenerator c = new CookieGenerator();
    c.setCookieName("savedUsers");
    c.setCookieHttpOnly(true);
    c.setCookieSecure(true);
    c.setCookieMaxAge((int) Duration.ofDays(365).getSeconds());
    return c;
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
        .csrf().disable()
        .authorizeHttpRequests((authorize) -> authorize
            .antMatchers(this.properties.getAuthnPath() + "/**").permitAll()
            .antMatchers("/idp/post", "/images/**", "/error", "/css/**", "/scripts/**", "/webjars/**").permitAll()
            .anyRequest().denyAll());

    return http.build();
  }

  /**
   * Gets a {@link WebSecurityCustomizer} that is configured to ignore static resources.
   *
   * @return a WebSecurityCustomizer
   */
//  @Bean
//  WebSecurityCustomizer webSecurityCustomizer() {
//    return (web) -> web.ignoring().antMatchers("/images/**", "/css/**", "/scripts/**", "/webjars/**");
//  }

}
