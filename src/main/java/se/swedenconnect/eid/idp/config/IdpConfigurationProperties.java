/*
 * Copyright 2016-2026 Sweden Connect
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

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;



/**
 * Configuration properties for our simulated IdP.
 *
 * @author Martin Lindström
 */
@ConfigurationProperties("authn")
public class IdpConfigurationProperties {

  /**
   * The name of the authentication provider.
   */
  private String providerName;

  /**
   * The authentication path. Where the Spring Security flow directs the user for authentication by our implementation.
   */
  private String authnPath;

  /**
   * The resume path. Where we redirect back the user after that we are done.
   */
  private String resumePath;

  /**
   * The supported LoA:s.
   */
  private List<String> supportedLoas;

  /**
   * The SAML entity categories this IdP declares.
   */
  private List<String> entityCategories;

  /**
   * Returns the name of the authentication provider.
   *
   * @return the provider name
   */
  public String getProviderName() {
    return this.providerName;
  }

  /**
   * Sets the name of the authentication provider.
   *
   * @param providerName the provider name
   */
  public void setProviderName(final String providerName) {
    this.providerName = providerName;
  }

  /**
   * Returns the authentication path.
   *
   * @return the authentication path
   */
  public String getAuthnPath() {
    return this.authnPath;
  }

  /**
   * Sets the authentication path.
   *
   * @param authnPath the authentication path
   */
  public void setAuthnPath(final String authnPath) {
    this.authnPath = authnPath;
  }

  /**
   * Returns the resume path.
   *
   * @return the resume path
   */
  public String getResumePath() {
    return this.resumePath;
  }

  /**
   * Sets the resume path.
   *
   * @param resumePath the resume path
   */
  public void setResumePath(final String resumePath) {
    this.resumePath = resumePath;
  }

  /**
   * Returns the supported LoA URIs.
   *
   * @return the list of supported LoA URIs
   */
  public List<String> getSupportedLoas() {
    return this.supportedLoas;
  }

  /**
   * Sets the supported LoA URIs.
   *
   * @param supportedLoas the list of supported LoA URIs
   */
  public void setSupportedLoas(final List<String> supportedLoas) {
    this.supportedLoas = supportedLoas;
  }

  /**
   * Returns the SAML entity categories declared by this IdP.
   *
   * @return the list of entity category URIs
   */
  public List<String> getEntityCategories() {
    return this.entityCategories;
  }

  /**
   * Sets the SAML entity categories declared by this IdP.
   *
   * @param entityCategories the list of entity category URIs
   */
  public void setEntityCategories(final List<String> entityCategories) {
    this.entityCategories = entityCategories;
  }

}
