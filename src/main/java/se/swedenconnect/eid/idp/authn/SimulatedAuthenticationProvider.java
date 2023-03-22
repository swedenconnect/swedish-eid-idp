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
package se.swedenconnect.eid.idp.authn;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.core.Authentication;

import lombok.NonNull;
import lombok.Setter;
import se.swedenconnect.eid.idp.users.SimulatedUser;
import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.sweid.saml2.authn.LevelOfAssuranceUris;
import se.swedenconnect.spring.saml.idp.attributes.UserAttribute;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthentication;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserDetails;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.AbstractUserRedirectAuthenticationProvider;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.ResumedAuthenticationToken;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;

/**
 * Simulated authentication provider.
 * 
 * @author Martin Lindström
 */
public class SimulatedAuthenticationProvider extends AbstractUserRedirectAuthenticationProvider {

  /** The supported LoA:s. */
  private final List<String> supportedAuthnContextUris;

  /** Declared/supported entity categories. */
  private final List<String> entityCategories;

  /** The provider name. */
  @Setter
  @NonNull
  private String name = "Simulated Authentication Provider";
  
  /**
   * Constructor.
   * 
   * @param authnPath the path to where we redirect the user for authentication
   * @param resumeAuthnPath the path that the authentication process uses to redirect the user back after a completed
   *          authentication
   * @param supportedAuthnContextUris the supported LoA:s
   * @param entityCategories declared/supported entity categories
   */
  public SimulatedAuthenticationProvider(final String authnPath, final String resumeAuthnPath,
      final List<String> supportedAuthnContextUris, final List<String> entityCategories) {
    super(authnPath, resumeAuthnPath);
    this.supportedAuthnContextUris = Optional.ofNullable(supportedAuthnContextUris)
        .filter(s -> !s.isEmpty())
        .orElseThrow(() -> new IllegalArgumentException("supportedAuthnContextUris must be set and be non-empty"));
    this.entityCategories = Objects.requireNonNull(entityCategories, "entityCategories must not be null");
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return this.name;
  }

  /** {@inheritDoc} */
  @Override
  public List<String> getSupportedAuthnContextUris() {
    return this.supportedAuthnContextUris;
  }

  /** {@inheritDoc} */
  @Override
  public List<String> getEntityCategories() {
    return this.entityCategories;
  }

  /** {@inheritDoc} */
  @Override
  public Saml2UserAuthentication resumeAuthentication(final ResumedAuthenticationToken token)
      throws Saml2ErrorStatusException {

    final SimulatedAuthenticationToken simAuth = SimulatedAuthenticationToken.class.cast(token.getAuthnToken());
    final SimulatedUser user = (SimulatedUser) simAuth.getDetails();

    final List<UserAttribute> attributes = List.of(
        new UserAttribute(AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_PERSONAL_IDENTITY_NUMBER, user.getPersonalNumber()),
        new UserAttribute(AttributeConstants.ATTRIBUTE_NAME_GIVEN_NAME,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_GIVEN_NAME, user.getGivenName()),
        new UserAttribute(AttributeConstants.ATTRIBUTE_NAME_SN,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_SN, user.getSurname()),
        new UserAttribute(AttributeConstants.ATTRIBUTE_NAME_DISPLAY_NAME,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_DISPLAY_NAME, user.getDisplayName()),
        new UserAttribute(AttributeConstants.ATTRIBUTE_NAME_DATE_OF_BIRTH,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_DATE_OF_BIRTH, user.getDateOfBirth()));

    final Saml2UserDetails userDetails = new Saml2UserDetails(attributes,
        AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER, simAuth.getLoa(),
        Instant.now(), token.getServletRequest().getRemoteAddr());

    final Saml2UserAuthentication userAuth = new Saml2UserAuthentication(userDetails);
    userAuth.setReuseAuthentication(true);

    return userAuth;
  }

  /** {@inheritDoc} */
  @Override
  public boolean supportsUserAuthenticationToken(final Authentication authentication) {
    return SimulatedAuthenticationToken.class.isInstance(authentication);
  }

}
