/*
 * Copyright 2016-2026 Sweden Connect
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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for UI settings.
 *
 * @author Martin Lindström
 */
@ConfigurationProperties("ui")
public class UiConfigurationProperties {

  /**
   * The UI language settings.
   */
  private List<Language> languages;

  /**
   * Returns the UI language settings.
   *
   * @return the list of configured languages
   */
  public List<Language> getLanguages() {
    return this.languages;
  }

  /**
   * Sets the UI language settings.
   *
   * @param languages the list of configured languages
   */
  public void setLanguages(final List<Language> languages) {
    this.languages = languages;
  }

  /**
   * UI language settings.
   */
  public static class Language {

    /**
     * The language tag.
     */
    private String tag;

    /**
     * The text associated with the language tag, e.g. English.
     */
    private String text;

    /**
     * Returns the language tag (e.g. {@code "en"}, {@code "sv"}).
     *
     * @return the language tag
     */
    public String getTag() {
      return this.tag;
    }

    /**
     * Sets the language tag (e.g. {@code "en"}, {@code "sv"}).
     *
     * @param tag the language tag
     */
    public void setTag(final String tag) {
      this.tag = tag;
    }

    /**
     * Returns the display text for the language, e.g. {@code "English"}.
     *
     * @return the display text
     */
    public String getText() {
      return this.text;
    }

    /**
     * Sets the display text for the language, e.g. {@code "English"}.
     *
     * @param text the display text
     */
    public void setText(final String text) {
      this.text = text;
    }
  }

}
