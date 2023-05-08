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
package se.swedenconnect.eid.idp.users;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.Setter;

/**
 * Representation of a user.
 *
 * @author Martin Lindström
 */
public class SimulatedUser implements UserDetails, Comparable<SimulatedUser> {

  private static final long serialVersionUID = 6822029385234222613L;

  /**
   * The personal identity number.
   */
  @Getter
  @Setter
  private String personalNumber;

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

  /**
   * The display name.
   */
  @Setter
  private String displayName;

  /**
   * The date of birth (YYYY-MM-DD).
   */
  @Setter
  private String dateOfBirth;

  /** {@inheritDoc} */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.emptyList();
  }

  /** {@inheritDoc} */
  @Override
  public String getPassword() {
    return "";
  }

  /** {@inheritDoc} */
  @Override
  public String getUsername() {
    return this.personalNumber;
  }

  /**
   * The date of birth (YYYY-MM-DD).
   *
   * @return the date of birth (YYYY-MM-DD).
   */
  public String getDateOfBirth() {
    if (this.dateOfBirth == null && this.personalNumber != null) {
      this.dateOfBirth = PersonalIdentityNumberSupport.getBirthDate(this.personalNumber);
    }
    return this.dateOfBirth;
  }

  /**
   * Gets the display name.
   * 
   * @return the display name
   */
  public String getDisplayName() {
    if (this.displayName == null) {
      this.displayName = String.format("%s %s", this.givenName, this.surname);
    }
    return this.displayName;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public String toViewString() {
    return String.format("%s (%s)", this.getDisplayName(), this.personalNumber);
  }

  @Override
  public int compareTo(final SimulatedUser o) {
    if (this.surname != null && o.surname != null) {
      return this.surname.compareTo(o.surname);
    }
    return this.surname == null ? 1 : -1;
  }

  public String encode() {
    return URLEncoder.encode(
        String.format("%s#%s#%s#%s", this.personalNumber, this.givenName, this.surname, this.getDateOfBirth()),
        StandardCharsets.UTF_8);
  }

  public static String encodeList(final List<SimulatedUser> list) {
    final StringBuffer sb = new StringBuffer();
    for (final SimulatedUser s : list) {
      if (sb.length() > 0) {
        sb.append(":::");
      }
      sb.append(s.encode());
    }
    return sb.toString();
  }

  public static SimulatedUser parse(final String s) {
    final String user = URLDecoder.decode(s, StandardCharsets.UTF_8);
    final String[] parts = user.split("#");
    if (parts.length < 3) {
      return null;
    }
    final SimulatedUser su = new SimulatedUser();
    su.setPersonalNumber(parts[0]);
    su.setGivenName(parts[1]);
    su.setSurname(parts[2]);
    if (parts.length > 3) {
      su.setDateOfBirth(parts[3]);
    }
    return su;
  }

  public static List<SimulatedUser> parseList(final String list) {
    if (list.trim().isEmpty()) {
      return Collections.emptyList();
    }
    final String parts[] = list.split(":::");
    final List<SimulatedUser> users = new ArrayList<>();
    for (final String p : parts) {
      final SimulatedUser su = parse(p);
      if (su != null) {
        users.add(su);
      }
    }
    return users;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.personalNumber);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    final SimulatedUser other = (SimulatedUser) obj;
    return Objects.equals(this.personalNumber, other.personalNumber);
  }

}
