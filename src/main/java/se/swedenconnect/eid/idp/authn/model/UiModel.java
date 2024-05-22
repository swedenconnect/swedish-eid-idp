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
package se.swedenconnect.eid.idp.authn.model;

import java.util.List;
import java.util.Objects;

import lombok.Data;
import se.swedenconnect.opensaml.sweid.saml2.authn.umsg.UserMessage;

/**
 * Model class for UI.
 * 
 * @author Martin Lindström
 */
@Data
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
   * Predicate that tells if the supplied ID is "selected".
   * 
   * @param id the ID to test
   * @return {@code true} if the ID is selected and {@code false} otherwise
   */
  public boolean isSelectedUser(final String id) {
    return Objects.equals(id, this.selectedUser);
  }
  
  public boolean isSelectedLoa(final String loa) {
    return Objects.equals(loa, this.selectedAuthnContextUri);
  }

}
