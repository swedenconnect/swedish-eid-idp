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

import java.util.List;
import java.util.Objects;

/**
 * Model class for UI.
 *
 * @author Martin Lindström
 */
public class UiModel {

  /** The SP display name (for the language of the current locale). */
  private String spDisplayName;

  /** The SP logo URL. */
  private String spLogoUrl;

  /** The user id for the user to pre-select. */
  private String selectedUser;

  /** Whether the selected user is "fixed" or not. */
  private boolean fixedSelectedUser = false;

  /** Possible authentication context URI:s. */
  private List<String> possibleAuthnContextUris;

  /** The authn context URI to pre-select. */
  private String selectedAuthnContextUri;

  /** Whether this is "authentication for signature". */
  private boolean signature = false;

  /** The SignMessage (HTML). */
  private String signMessage;

  /** The UserMessage (HTML). */
  private String userMessage;

  /**
   * Returns the SP display name for the current locale.
   *
   * @return the SP display name
   */
  public String getSpDisplayName() {
    return this.spDisplayName;
  }

  /**
   * Sets the SP display name for the current locale.
   *
   * @param spDisplayName the SP display name
   */
  public void setSpDisplayName(final String spDisplayName) {
    this.spDisplayName = spDisplayName;
  }

  /**
   * Returns the SP logo URL.
   *
   * @return the SP logo URL
   */
  public String getSpLogoUrl() {
    return this.spLogoUrl;
  }

  /**
   * Sets the SP logo URL.
   *
   * @param spLogoUrl the SP logo URL
   */
  public void setSpLogoUrl(final String spLogoUrl) {
    this.spLogoUrl = spLogoUrl;
  }

  /**
   * Returns the user id to pre-select.
   *
   * @return the user id
   */
  public String getSelectedUser() {
    return this.selectedUser;
  }

  /**
   * Sets the user id to pre-select.
   *
   * @param selectedUser the user id
   */
  public void setSelectedUser(final String selectedUser) {
    this.selectedUser = selectedUser;
  }

  /**
   * Returns whether the selected user is fixed (cannot be changed).
   *
   * @return {@code true} if the selected user is fixed
   */
  public boolean isFixedSelectedUser() {
    return this.fixedSelectedUser;
  }

  /**
   * Sets whether the selected user is fixed (cannot be changed).
   *
   * @param fixedSelectedUser {@code true} if the selected user is fixed
   */
  public void setFixedSelectedUser(final boolean fixedSelectedUser) {
    this.fixedSelectedUser = fixedSelectedUser;
  }

  /**
   * Returns the possible authentication context URIs.
   *
   * @return the list of authentication context URIs
   */
  public List<String> getPossibleAuthnContextUris() {
    return this.possibleAuthnContextUris;
  }

  /**
   * Sets the possible authentication context URIs.
   *
   * @param possibleAuthnContextUris the list of authentication context URIs
   */
  public void setPossibleAuthnContextUris(final List<String> possibleAuthnContextUris) {
    this.possibleAuthnContextUris = possibleAuthnContextUris;
  }

  /**
   * Returns the authentication context URI to pre-select.
   *
   * @return the authentication context URI
   */
  public String getSelectedAuthnContextUri() {
    return this.selectedAuthnContextUri;
  }

  /**
   * Sets the authentication context URI to pre-select.
   *
   * @param selectedAuthnContextUri the authentication context URI
   */
  public void setSelectedAuthnContextUri(final String selectedAuthnContextUri) {
    this.selectedAuthnContextUri = selectedAuthnContextUri;
  }

  /**
   * Returns whether this is authentication for signature.
   *
   * @return {@code true} if this is a signature authentication request
   */
  public boolean isSignature() {
    return this.signature;
  }

  /**
   * Sets whether this is authentication for signature.
   *
   * @param signature {@code true} if this is a signature authentication request
   */
  public void setSignature(final boolean signature) {
    this.signature = signature;
  }

  /**
   * Returns the SignMessage HTML to display.
   *
   * @return the SignMessage HTML, or {@code null} if none
   */
  public String getSignMessage() {
    return this.signMessage;
  }

  /**
   * Sets the SignMessage HTML to display.
   *
   * @param signMessage the SignMessage HTML
   */
  public void setSignMessage(final String signMessage) {
    this.signMessage = signMessage;
  }

  /**
   * Returns the UserMessage HTML to display.
   *
   * @return the UserMessage HTML, or {@code null} if none
   */
  public String getUserMessage() {
    return this.userMessage;
  }

  /**
   * Sets the UserMessage HTML to display.
   *
   * @param userMessage the UserMessage HTML
   */
  public void setUserMessage(final String userMessage) {
    this.userMessage = userMessage;
  }

  /**
   * Predicate that tells if the supplied ID is "selected".
   *
   * @param id the ID to test
   * @return {@code true} if the ID is selected and {@code false} otherwise
   */
  public boolean isSelectedUser(final String id) {
    return Objects.equals(id, this.selectedUser);
  }

  public boolean isSelectedLoa(final String loa) {
    return Objects.equals(loa, this.selectedAuthnContextUri);
  }

}
