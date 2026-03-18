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


/**
 * Configuration properties for Tomcat AJP.
 *
 * @author Martin Lindström
 */
public class TomcatAjpConfigurationProperties {

  /** Is AJP enabled? */
  private boolean enabled = false;

  /** The Tomcat AJP port. */
  private int port = 8009;

  /** AJP secret. */
  private String secret;

  /** Is AJP secret required? */
  private boolean secretRequired = false;

  /**
   * Returns whether AJP is enabled.
   *
   * @return {@code true} if AJP is enabled
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * Sets whether AJP is enabled.
   *
   * @param enabled {@code true} to enable AJP
   */
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns the Tomcat AJP port.
   *
   * @return the AJP port
   */
  public int getPort() {
    return this.port;
  }

  /**
   * Sets the Tomcat AJP port.
   *
   * @param port the AJP port
   */
  public void setPort(final int port) {
    this.port = port;
  }

  /**
   * Returns the AJP secret.
   *
   * @return the AJP secret, or {@code null} if not set
   */
  public String getSecret() {
    return this.secret;
  }

  /**
   * Sets the AJP secret.
   *
   * @param secret the AJP secret
   */
  public void setSecret(final String secret) {
    this.secret = secret;
  }

  /**
   * Returns whether the AJP secret is required.
   *
   * @return {@code true} if the AJP secret is required
   */
  public boolean isSecretRequired() {
    return this.secretRequired;
  }

  /**
   * Sets whether the AJP secret is required.
   *
   * @param secretRequired {@code true} if the AJP secret is required
   */
  public void setSecretRequired(final boolean secretRequired) {
    this.secretRequired = secretRequired;
  }

}
