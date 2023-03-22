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

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Model class for a user that is selected in the UI, i.e., the user that we are simulating authentication for.
 * 
 * @author Martin Lindström
 */
public class SelectedUserModel {

  /**
   * The personal identity number.
   */
  @Setter
  private String personalIdentityNumber;
  
  @Setter
  private String customPersonalIdentityNumber;

  /**
   * The level of assurance.
   */
  @Getter
  @Setter
  private String loa;

  /**
   * The given name.
   */
  @Getter
  @Setter
  private String givenName;

  /**
   * The surname.
   */
  @Getter
  @Setter
  private String surname;

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
    return StringUtils.hasText(this.getPersonalIdentityNumber()) && StringUtils.hasText(this.givenName) && StringUtils.hasText(this.surname);
  }

}
