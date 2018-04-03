/*
 * Copyright 2016-2018 E-legitimationsnämnden
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
package se.elegnamnden.eid.idp.authn.model;

import lombok.Data;

/**
 * Represents a simulated user.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se) 
 */
@Data
public class SimulatedUser implements Comparable<SimulatedUser> {

  /** The personal identity number. */  
  private String personalIdentityNumber;

  /** The user given name. */
  private String givenName;

  /** The user surname. */
  private String surname;

  /** The user display name. */
  private String displayName;

  /**
   * Default constructor.
   */
  public SimulatedUser() {
  }

  public SimulatedUser(String personalIdentityNumber, String givenName, String surname) {
    this.personalIdentityNumber = personalIdentityNumber;
    this.givenName = givenName;
    this.surname = surname;
  }

  public SimulatedUser(String personalIdentityNumber, String givenName, String surname, String displayName) {
    this(personalIdentityNumber, givenName, surname);
    this.displayName = displayName;
  }

  public String getDisplayName() {
    if (this.displayName == null) {
      StringBuffer sb = new StringBuffer();
      if (this.givenName != null) {
        sb.append(this.givenName);
      }
      if (this.surname != null) {
        if (sb.length() > 0) {
          sb.append(" ");
        }
        sb.append(this.surname);
      }
      return sb.length() > 0 ? sb.toString() : null;
    }
    return this.displayName;
  }

  public String getUiDisplayName() {
    return String.format("%s (%s)", this.getDisplayName(), this.getPersonalIdentityNumber());
  }

  @Override
  public int compareTo(SimulatedUser o) {
    if (this.surname != null && o.surname != null) {
      return this.surname.compareTo(o.surname);
    }
    return this.surname == null ? 1 : -1;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("personalIdentityNumber='%s', givenName='%s', surname='%s', displayName='%s'",
      this.personalIdentityNumber, this.givenName, this.surname, this.displayName);
  }

}
