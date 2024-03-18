/*
 * Copyright 2023-2024 Sweden Connect
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
package se.swedenconnect.eid.idp.authn;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;

import se.swedenconnect.opensaml.sweid.saml2.signservice.dss.SignMessageMimeTypeEnum;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatus;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;
import se.swedenconnect.spring.saml.idp.extensions.SignatureMessagePreprocessor;

/**
 * Transforms all sign messages to safe HTML for later inclusion in the IdP UI.
 *
 * @author Martin Lindström
 */
@Component
public class HtmlSignMessagePreProcessor implements SignatureMessagePreprocessor {

  /** The allowed HTML tags that may appear in a sign message. */
  private static final String[] allowedHtmlTags = new String[] { "b", "blockquote", "br", "caption", "cite", "code",
      "dd", "div", "dl", "dt", "em", "h1", "h2", "h3", "h4", "h5", "h6",
      "i", "li", "ol", "p", "pre", "q", "small", "span", "strike", "strong",
      "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "u", "ul" };

  /** The HTML whitelist used when cleaning supplied HTML. */
  private final Safelist htmlWhitelist;

  /** The HTML whitelist when checking for illegal HTML tags. */
  private final PreprocessingWhitelist htmlPreprocessingWhitelist;

  /** Markdown parser. */
  private final Parser markdownParser;

  /** HTML renderer for markdown. */
  private final HtmlRenderer markdownHtmlRenderer;

  /**
   * Constructor.
   */
  public HtmlSignMessagePreProcessor() {
    // HTML
    this.htmlWhitelist = new Safelist();
    this.htmlWhitelist.addTags(allowedHtmlTags);

    this.htmlPreprocessingWhitelist = new PreprocessingWhitelist();
    this.htmlPreprocessingWhitelist.addTags(allowedHtmlTags);

    // Markdown
    final MutableDataSet options = new MutableDataSet();
    options.setFrom(ParserEmulationProfile.MARKDOWN);
    options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));

    this.markdownParser = Parser.builder(options).build();
    this.markdownHtmlRenderer = HtmlRenderer.builder(options).build();
  }

  /** {@inheritDoc} */
  @Override
  public String processSignMessage(final String encodedText, final SignMessageMimeTypeEnum messageType)
      throws Saml2ErrorStatusException {

    final String input = new String(Base64.getDecoder().decode(encodedText), StandardCharsets.UTF_8);
    String htmlMessage;

    // Text
    if (messageType == null || messageType == SignMessageMimeTypeEnum.TEXT) {

      htmlMessage = "<div style='font-family: \"Lucida Console\", Monaco, monospace'>";

      // Filter to protect against XSS
      htmlMessage += StringEscapeUtils.escapeHtml4(input);

      // Replace NL with <br />
      htmlMessage = htmlMessage.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");

      // Replace tabs with &emsp;
      htmlMessage = htmlMessage.replaceAll("\t", "&emsp;");

      htmlMessage += "</div>";
    }
    // HTML
    else if (messageType == SignMessageMimeTypeEnum.TEXT_HTML) {
      htmlMessage = this.validateAndCleanHtml(input);
    }
    // Markdown
    else if (messageType == SignMessageMimeTypeEnum.TEXT_MARKDOWN) {
      htmlMessage = this.markdownToHtml(input);
    }
    else {
      throw new Saml2ErrorStatusException(Saml2ErrorStatus.SIGN_MESSAGE, "Unsupported SignMessage type");
    }

    return htmlMessage;
  }

  /**
   * Validates that the supplied HTML does not contain any non-allowed HTML tags and cleans the supplied HTML from any
   * supplied attributes.
   *
   * @param html the HTML to validate and clean
   * @return validated and cleaned HTML
   * @throws Saml2ErrorStatusException if the HTML contains illegal tags
   */
  private String validateAndCleanHtml(final String html) throws Saml2ErrorStatusException {

    // First validate the HTML based on allowed tags.
    // At this point we allow any attributes, but will fail on non-allowed
    // tags.
    //
    if (!Jsoup.isValid(html, this.htmlPreprocessingWhitelist)) {
      throw new Saml2ErrorStatusException(Saml2ErrorStatus.SIGN_MESSAGE,
          "SignMessage HTML is not allowed - contains invalid HTML or non-allowed HTML tags");
    }

    // Then clean the HTML (removing attributes).
    //
    return Jsoup.clean(html, this.htmlWhitelist);
  }

  /**
   * Parses the supplied markdown into validated and cleaned HTML.
   *
   * @param markdown the markdown text
   * @return validated and cleaned HTML
   * @throws Saml2ErrorStatusException if the generated HTML is illegal
   */
  private String markdownToHtml(final String markdown) throws Saml2ErrorStatusException {

    // First translate the Markdown into HTML.
    //
    final Document document = this.markdownParser.parse(markdown);
    final String html = this.markdownHtmlRenderer.render(document);

    // Next, validate and clean the rendered HTML.
    //
    try {
      return this.validateAndCleanHtml(html);
    }
    catch (final Exception e) {
      throw new Saml2ErrorStatusException(Saml2ErrorStatus.SIGN_MESSAGE, "SignMessage markdown generated illegal HTML");
    }
  }

  /**
   * A Jsoup {@link Safelist} class that will allow all attributes. We use this instance when checking for invalid HTML
   * tags.
   */
  private static class PreprocessingWhitelist extends Safelist {

    public PreprocessingWhitelist() {
      super();
    }

    @Override
    public boolean isSafeAttribute(final String tagName, final Element el, final Attribute attr) {
      return true;
    }
  }

}
