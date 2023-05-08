/*
 * Copyright 2022-2023 Sweden Connect
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

/**
 * Support functions for handling personal identity numbers.
 * 
 * @author Martin Lindström
 */
public class PersonalIdentityNumberSupport {

  /**
   * Predicate that tells if the supplied personal identity number is a Swedish coordination number (samordningsnummer).
   * 
   * @param id
   *          the personal identity number
   * @return true if the number is a coordination number and false otherwise
   */
  public static boolean isCoordinationNumber(final String id) {
    if (id.length() != 12) {
      return false;
    }
    try {
      final Integer day = Integer.parseInt(id.substring(6, 8));
      return (day >= 61);
    }
    catch (final Exception e) {
      return false;
    }
  }

  /**
   * Given a Swedish personal identity number or "samordningsnummer" a person's date of birth is returned.
   *
   * @param personalIdentityNumber
   *          the personal identity number
   * @return the birth date on the format YYYY-MM-DD
   */
  public static String getBirthDate(final String personalIdentityNumber) {
    final Integer day = Integer.parseInt(personalIdentityNumber.substring(6, 8));
    return String.format("%s-%s-%02d", personalIdentityNumber.substring(0, 4), personalIdentityNumber.substring(4, 6),
      day > 60 ? day - 60 : day);
  }

  private PersonalIdentityNumberSupport() {
  }

}
