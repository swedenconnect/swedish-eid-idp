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
package se.elegnamnden.eid.idp.authn.controller;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdui.Logo;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.LocalizedName;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import se.elegnamnden.eid.idp.authn.model.SpInfo;
import se.litsec.opensaml.saml2.metadata.MetadataUtils;

/**
 * Handler class for constructing a {@link SpInfo} model object.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 * @author Stefan Santesson (stefan@aaa-sec.com)
 */
public class SpInfoHandler {

  /**
   * Builds a {@link SpInfo} model object given an entity's metadata entry.
   * 
   * @param metadata
   *          the metadata
   * @param language
   *          the preferred language
   * @param fallbackLanguages
   *          fallback languages
   * @return a {@code SpInfo} object
   */
  public static SpInfo buildSpInfo(EntityDescriptor metadata, String language, List<String> fallbackLanguages) {

    if (metadata == null) {
      return null;
    }
    SPSSODescriptor descriptor = metadata.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
    if (descriptor == null) {
      return null;
    }
    UIInfo uiInfo = MetadataUtils.getMetadataExtension(descriptor.getExtensions(), UIInfo.class).orElse(null);
    if (uiInfo == null) {
      return null;
    }

    SpInfo spInfo = new SpInfo();

    spInfo.setDisplayName(getLocalizedName(uiInfo.getDisplayNames(), language, fallbackLanguages));
    if (spInfo.getDisplayName() == null) {
      // OK, it seems like the SP did not specify a UIInfo. Pick the name from the organization element instead.
      //
      if (metadata.getOrganization() != null) {
        spInfo.setDisplayName(getLocalizedName(metadata.getOrganization().getDisplayNames(), language, fallbackLanguages));
      }
    }
    spInfo.setDescription(getLocalizedName(uiInfo.getDescriptions(), language, fallbackLanguages));

    // Try to find something larger than 40px and less than 100px first
    //
    Logo small = null;
    Logo large = null;
    for (Logo logo : uiInfo.getLogos()) {
      if (logo.getHeight() == null) {
        continue;
      }
      if (logo.getHeight() > 40 && logo.getHeight() < 100) {
        spInfo.setDefaultLogoUrl(logo.getURL());
        break;
      }
      else if (logo.getHeight() < 40) {
        if (small == null || (small != null && logo.getHeight() > small.getHeight())) {
          small = logo;
        }
      }
      else if (logo.getHeight() > 100) {
        if (large == null || (large != null && logo.getHeight() < large.getHeight())) {
          large = logo;
        }
      }
    }
    if (spInfo.getDefaultLogoUrl() == null) {
      if (large != null) {
        spInfo.setDefaultLogoUrl(large.getURL());
      }
      else if (small != null) {
        spInfo.setDefaultLogoUrl(small.getURL());
      }
      else if (!uiInfo.getLogos().isEmpty()) {
        spInfo.setDefaultLogoUrl(uiInfo.getLogos().get(0).getURL());
      }
    }

    return spInfo;
  }

  private static <T extends LocalizedName> String getLocalizedName(List<T> names, String language, List<String> fallbackLanguages) {
    if (names == null || names.isEmpty()) {
      return null;
    }

    Function<String, String> getDisplayName = (lang) -> names.stream()
      .filter(n -> lang.equals(n.getXMLLang()))
      .map(LocalizedName::getValue)
      .findFirst()
      .orElse(null);

    String displayName = getDisplayName.apply(language);
    if (displayName != null) {
      return displayName;
    }
    displayName = fallbackLanguages.stream()
      .filter(l -> !language.equals(l))
      .map(getDisplayName)
      .filter(Objects::nonNull)
      .findFirst()
      .orElse(null);

    if (displayName != null) {
      return displayName;
    }
    // OK, then just pick the first.
    return names.get(0).getValue();
  }
  
  public static class SpInfoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
      return SpInfo.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
    }
    
  }

}
