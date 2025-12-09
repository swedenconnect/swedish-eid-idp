/*
 * Copyright 2023-2025 Sweden Connect
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

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseCookie.ResponseCookieBuilder;

import java.time.Duration;

/**
 * Helper class for handling cookies.
 *
 * @author Martin Lindström
 */
public class CookieGenerator {

  private final ResponseCookieBuilder cookieBuilder;

  private final String name;

  /**
   * Constructor.
   *
   * @param name cookie name
   * @param path cookie path
   * @param maxAge max age
   */
  public CookieGenerator(final String name, final String path, final Duration maxAge) {
    this.name = name;
    this.cookieBuilder = ResponseCookie.from(name)
        .path(path)
        .httpOnly(true)
        .secure(true)
        .maxAge(maxAge);
  }

  /**
   * Gets the cookie name.
   *
   * @return the cookie name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Adds a cookie to the response.
   *
   * @param value the value of the cookie
   * @param response the HTTP servlet response
   */
  public void addCookie(final String value, final HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE,
        this.cookieBuilder.value(value).build().toString());
  }

  /**
   * Clears the cookie.
   *
   * @param response the HTTP servlet response
   */
  public void clearCookie(final HttpServletResponse response) {
    response.setHeader(HttpHeaders.SET_COOKIE,
        this.cookieBuilder.maxAge(0).build().toString());
  }

}
