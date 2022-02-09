/*
 * Copyright 2016-2022 Sweden Connect
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
  
  /** The organizational identity (minus the organization number). */
  private String orgIdentity;
  
  /** The organization identifier (number). */
  private String organizationIdentifier;
  
  /** The organization name. */
  private String organizationName;
  
  /** The organization display name. */
  private String organizationDisplayName;
  
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
  
  public boolean isOrganizationalUser() {
    return this.orgIdentity != null && this.organizationIdentifier != null
        && this.organizationName != null;
  }
  
  public String getDisplayName(boolean requestOrgId) {
    if (requestOrgId && this.organizationDisplayName != null) {
      return this.organizationDisplayName;
    }
    
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
  
  public String getOrgAffiliation() {
    if (this.orgIdentity != null && this.organizationIdentifier != null) {
      return String.format("%s@%s", this.orgIdentity, this.organizationIdentifier);
    }
    return null;
  }

  public String encode() {
    try {
      return URLEncoder.encode(String.format("%s#%s#%s", this.personalIdentityNumber, this.givenName, this.surname), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String encodeList(List<SimulatedUser> list) {
    StringBuffer sb = new StringBuffer();
    for (SimulatedUser s : list) {
      if (sb.length() > 0) {
        sb.append(":::");
      }
      sb.append(s.encode());
    }
    return sb.toString();
  }

  public static SimulatedUser parse(String s) {
    try {
      String user = URLDecoder.decode(s, "UTF-8");
      String[] parts = user.split("#");
      if (parts.length != 3) {
        return null;
      }
      return new SimulatedUser(parts[0], parts[1], parts[2]);
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<SimulatedUser> parseList(String list) {
    if (list.trim().isEmpty()) {
      return Collections.emptyList();
    }
    String parts[] = list.split(":::");
    List<SimulatedUser> users = new ArrayList<>();
    for (String p : parts) {
      SimulatedUser su = parse(p);
      if (su != null) {
        users.add(su);
      }
    }
    return users;
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    SimulatedUser other = (SimulatedUser) obj;
    if (this.givenName == null) {
      if (other.givenName != null) {
        return false;
      }
    }
    else if (!this.givenName.equals(other.givenName)) {
      return false;
    }
    if (this.personalIdentityNumber == null) {
      if (other.personalIdentityNumber != null) {
        return false;
      }
    }
    else if (!this.personalIdentityNumber.equals(other.personalIdentityNumber)) {
      return false;
    }
    if (this.surname == null) {
      if (other.surname != null) {
        return false;
      }
    }
    else if (!this.surname.equals(other.surname)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.givenName == null) ? 0 : this.givenName.hashCode());
    result = prime * result + ((this.personalIdentityNumber == null) ? 0 : this.personalIdentityNumber.hashCode());
    result = prime * result + ((this.surname == null) ? 0 : this.surname.hashCode());
    return result;
  }

}
