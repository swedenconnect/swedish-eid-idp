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
package se.swedenconnect.eid.idp.authn.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

/**
 * Model class for a user that is selected in the UI, i.e., the user that we are simulating authentication for.
 *
 * @author Martin Lindström
 */
public class SelectedUserModel {

  /** The personal identity number.*/
  private String personalIdentityNumber;

  /** A custom personal identity number entered manually in the advanced view. */
  private String customPersonalIdentityNumber;

  /** The level of assurance. */
  private String loa;

  /** The given name. */
  private String givenName;

  /** The surname. */
  private String surname;

  /** Whether the SignMessage was displayed. */
  private boolean signMessageDisplayed;

  /** Main error code to simulate. */
  private String mainError;

  /** Subordinate error code to simulate. */
  private String subError;

  /** Error message to simulate. */
  private String errorMessage;

  /**
   * Sets the personal identity number selected from the drop-down.
   *
   * @param personalIdentityNumber the personal identity number
   */
  public void setPersonalIdentityNumber(final @NonNull String personalIdentityNumber) {
    this.personalIdentityNumber = personalIdentityNumber;
  }

  /**
   * Sets the personal identity number entered manually in the advanced view.
   *
   * @param customPersonalIdentityNumber the personal identity number
   */
  public void setCustomPersonalIdentityNumber(final @Nullable String customPersonalIdentityNumber) {
    this.customPersonalIdentityNumber = customPersonalIdentityNumber;
  }

  /**
   * Returns the level of assurance URI.
   *
   * @return the LoA URI
   */
  public String getLoa() {
    return this.loa;
  }

  /**
   * Sets the level of assurance URI.
   *
   * @param loa the LoA URI
   */
  public void setLoa(final String loa) {
    this.loa = loa;
  }

  /**
   * Returns the given name.
   *
   * @return the given name
   */
  public String getGivenName() {
    return this.givenName;
  }

  /**
   * Sets the given name.
   *
   * @param givenName the given name
   */
  public void setGivenName(final String givenName) {
    this.givenName = givenName;
  }

  /**
   * Returns the surname.
   *
   * @return the surname
   */
  public String getSurname() {
    return this.surname;
  }

  /**
   * Sets the surname.
   *
   * @param surname the surname
   */
  public void setSurname(final String surname) {
    this.surname = surname;
  }

  /**
   * Returns whether the SignMessage was displayed.
   *
   * @return {@code true} if the SignMessage was displayed
   */
  public boolean isSignMessageDisplayed() {
    return this.signMessageDisplayed;
  }

  /**
   * Sets whether the SignMessage was displayed.
   *
   * @param signMessageDisplayed {@code true} if the SignMessage was displayed
   */
  public void setSignMessageDisplayed(final boolean signMessageDisplayed) {
    this.signMessageDisplayed = signMessageDisplayed;
  }

  /**
   * Returns the main error code to simulate.
   *
   * @return the main error code
   */
  public String getMainError() {
    return this.mainError;
  }

  /**
   * Sets the main error code to simulate.
   *
   * @param mainError the main error code
   */
  public void setMainError(final String mainError) {
    this.mainError = mainError;
  }

  /**
   * Returns the subordinate error code to simulate.
   *
   * @return the subordinate error code
   */
  public String getSubError() {
    return this.subError;
  }

  /**
   * Sets the subordinate error code to simulate.
   *
   * @param subError the subordinate error code
   */
  public void setSubError(final String subError) {
    this.subError = subError;
  }

  /**
   * Returns the error message to simulate.
   *
   * @return the error message
   */
  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * Sets the error message to simulate.
   *
   * @param errorMessage the error message
   */
  public void setErrorMessage(final String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getPersonalIdentityNumber() {
    return StringUtils.hasText(this.personalIdentityNumber)
        ? this.personalIdentityNumber
        : this.customPersonalIdentityNumber;
  }

  /**
   * Is this a "custom user"?, i.e., created from the Advanced-link.
   *
   * @return {@code true} if created in the view, and {@code false} if a user from the drop-down menu was selected
   */
  public boolean isCustom() {
    return StringUtils.hasText(this.getPersonalIdentityNumber()) && StringUtils.hasText(this.givenName)
        && StringUtils.hasText(this.surname);
  }

}
