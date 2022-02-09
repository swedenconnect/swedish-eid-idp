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

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import se.swedenconnect.eid.idp.authn.model.UiLanguage;

/**
 * Handler for which language the UI uses based on user preferences.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 * @author Stefan Santesson (stefan@aaa-sec.com)
 */
public class UiLanguageHandler implements InitializingBean {

  /** Logging instance. */
  private final Logger log = LoggerFactory.getLogger(UiLanguageHandler.class);

  /** List of the languages we support. */
  private List<UiLanguage> languages;

  /**
   * Returns a list of languages to display as selectable in the UI. The method will not include the language for the
   * currently used language.
   * 
   * @return a list of language model objects
   */
  public List<UiLanguage> getUiLanguages() {
    Locale locale = LocaleContextHolder.getLocale();

    return this.languages.stream()
      .filter(lang -> !lang.getLanguageTag().equals(locale.getLanguage()))
      .collect(Collectors.toList());
  }

  /**
   * Updates the UI language for the current user.
   * 
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   * @param language
   *          the language tag
   */
  public void setUiLanguage(HttpServletRequest request, HttpServletResponse response, String language) {

    try {
      Locale locale = Locale.forLanguageTag(language);

      LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
      if (localeResolver != null) {
        localeResolver.setLocale(request, response, locale);
      }
      LocaleContextHolder.setLocale(locale);
    }
    catch (Exception e) {
      log.error("Failed to save selected UI language", e);
    }
  }

  /**
   * Assigns the supported languages.
   * 
   * @param languages
   *          list of supported languages
   */
  public void setLanguages(List<UiLanguage> languages) {
    this.languages = languages;
  }

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notEmpty(this.languages, "The property 'languages' must be assigned");
  }

}
