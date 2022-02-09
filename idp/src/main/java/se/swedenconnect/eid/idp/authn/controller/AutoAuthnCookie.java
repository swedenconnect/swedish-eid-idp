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
package se.swedenconnect.eid.idp.authn.controller;

/**
 * Representation of the Auto authentication cookie. This is used when we are running the IdP in a UI less mode which
 * can be useful for automated testing.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 * @author Stefan Santesson (stefan@aaa-sec.com)
 */
public class AutoAuthnCookie {

  /** The name of the auto-auth cookie. */
  public static final String AUTO_AUTHN_COOKIE_NAME = "autoAuthUser";
  
  /** The user id stored in the cookie. */
  private String personalIdentityNumber;

  /**
   * Default constructor.
   */
  public AutoAuthnCookie() {
  }

  /**
   * Parses the cookie value into a cookie instance.
   * 
   * @param value
   *          the cookie contents
   * @return an {@code  AutoAuthnCookie} instance, or {@code null} if it cannot be parsed
   */
  public static AutoAuthnCookie parse(String value) {
    if (value == null) {
      return null;
    }
    AutoAuthnCookie cookie = new AutoAuthnCookie();
    cookie.personalIdentityNumber = value;
    return cookie;
  }

  /**
   * Returns the cookie value as a string.
   * 
   * @return the cookie value
   */
  public String getCookieValue() {
    return this.personalIdentityNumber;
  }

  /**
   * Returns the personal identity number for the user id stored in the cookie.
   * 
   * @return the personal identity number
   */
  public String getPersonalIdentityNumber() {
    return this.personalIdentityNumber;
  }

  /**
   * Assigns the personal identity number for the cookie.
   * 
   * @param personalIdentityNumber
   *          the id to assign
   */
  public void setPersonalIdentityNumber(String personalIdentityNumber) {
    this.personalIdentityNumber = personalIdentityNumber;
  }

}
