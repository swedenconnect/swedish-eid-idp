/*
 * Copyright 2022 Sweden Connect
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

import java.util.List;

import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import se.litsec.swedisheid.opensaml.saml2.authentication.LevelofAssuranceAuthenticationContextURI;
import se.litsec.swedisheid.opensaml.saml2.metadata.entitycategory.EntityCategoryConstants;
import se.litsec.swedisheid.opensaml.saml2.metadata.entitycategory.EntityCategoryMetadataHelper;

/**
 * Support functions for handling personal identity numbers.
 * 
 * @author Martin Lindström (martin@litsec.se)
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

  public static boolean validForSP(final String id, final EntityDescriptor spMetadata, final String loa) {
    if (!isCoordinationNumber(id)) {
      return true;
    }
    List<String> entityCategories = EntityCategoryMetadataHelper.getEntityCategories(spMetadata);
    if (entityCategories.contains(EntityCategoryConstants.GENERAL_CATEGORY_ACCEPTS_COORDINATION_NUMBER.getUri())) {
      return true;
    }
    else {
      if (loa == null) {
        if (entityCategories.contains(EntityCategoryConstants.SERVICE_ENTITY_CATEGORY_LOA2_NAME.getUri())
            || entityCategories.contains(EntityCategoryConstants.SERVICE_ENTITY_CATEGORY_LOA3_NAME.getUri())) {
          // OK, we won't release the number, but can release other attributes ...
          return true;
        }
      }
      else if (LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_LOA3.equals(loa)
          || LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_UNCERTIFIED_LOA3.equals(loa)) {
        return entityCategories.contains(EntityCategoryConstants.SERVICE_ENTITY_CATEGORY_LOA3_NAME.getUri());
      }
      else if (LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_LOA2.equals(loa)) {
        return entityCategories.contains(EntityCategoryConstants.SERVICE_ENTITY_CATEGORY_LOA2_NAME.getUri());
      }
      
      // TODO: orgAffiliation
      return false;
    }
  }
  
  public static boolean canIssuePersonalIdentityNumber(final String id, final EntityDescriptor spMetadata, final String loa) {
    if (!isCoordinationNumber(id)) {
      return true;
    }
    List<String> entityCategories = EntityCategoryMetadataHelper.getEntityCategories(spMetadata);
    if (entityCategories.contains(EntityCategoryConstants.GENERAL_CATEGORY_ACCEPTS_COORDINATION_NUMBER.getUri())) {
      return true;
    }
    else {
      return false;
    }
  }

  private PersonalIdentityNumberSupport() {
  }

}
