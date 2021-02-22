/*
 * Copyright 2016-2021 Litsec AB
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
package se.elegnamnden.eid.idp.authn.service;

import java.util.Arrays;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;

import se.litsec.shibboleth.idp.authn.service.SignMessageContentException;
import se.litsec.shibboleth.idp.authn.service.SignMessagePreProcessor;
import se.litsec.swedisheid.opensaml.saml2.signservice.dss.SignMessageMimeTypeEnum;

/**
 * A {@link SignMessagePreProcessor} that transform all sign messages to safe HTML for later
 * inclusion in the IdP UI.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class DefaultSignMessagePreProcessor implements SignMessagePreProcessor {
  
  /** The allowed HTML tags that may appear in a sign message. */
  private static final String[] allowedHtmlTags = new String[] { "b", "blockquote", "br", "caption", "cite", "code",
      "dd", "div", "dl", "dt", "em", "h1", "h2", "h3", "h4", "h5", "h6",
      "i", "li", "ol", "p", "pre", "q", "small", "span", "strike", "strong",
      "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "u", "ul" };

  /** The HTML whitelist used when cleaning supplied HTML. */
  private Whitelist htmlWhitelist;

  /** The HTML whitelist when checking for illegal HTML tags. */
  private PreprocessingWhitelist htmlPreprocessingWhitelist;

  /** Markdown parser. */
  private Parser markdownParser;

  /** HTML renderer for markdown. */
  private HtmlRenderer markdownHtmlRenderer;  

  /**
   * Constructor.
   */
  public DefaultSignMessagePreProcessor() {
    // HTML
    this.htmlWhitelist = new Whitelist();
    this.htmlWhitelist.addTags(allowedHtmlTags);

    this.htmlPreprocessingWhitelist = new PreprocessingWhitelist();
    this.htmlPreprocessingWhitelist.addTags(allowedHtmlTags);

    // Markdown
    MutableDataSet options = new MutableDataSet();
    options.setFrom(ParserEmulationProfile.MARKDOWN);
    options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));

    this.markdownParser = Parser.builder(options).build();
    this.markdownHtmlRenderer = HtmlRenderer.builder(options).build();    
  }

  /** {@inheritDoc} */
  @Override
  public String processSignMessage(final String clearText, final SignMessageMimeTypeEnum messageType) throws SignMessageContentException {
    
    String htmlMessage;

    // Text
    if (messageType == null || messageType == SignMessageMimeTypeEnum.TEXT) {
      
      htmlMessage = "<div style='font-family: \"Lucida Console\", Monaco, monospace'>";

      // Filter to protect against XSS
      htmlMessage += StringEscapeUtils.escapeHtml(clearText);

      // Replace NL with <br />
      htmlMessage = htmlMessage.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");

      // Replace tabs with &emsp;
      htmlMessage = htmlMessage.replaceAll("\t", "&emsp;");
      
      htmlMessage += "</div>";
    }
    // HTML
    else if (messageType == SignMessageMimeTypeEnum.TEXT_HTML) {
      htmlMessage = this.validateAndCleanHtml(clearText);
    }
    // Markdown
    else if (messageType == SignMessageMimeTypeEnum.TEXT_MARKDOWN) {     
      htmlMessage = this.markdownToHtml(clearText);
    }
    else {
      throw new SignMessageContentException("Unrecognized SignMessage type " + messageType);
    }

    return htmlMessage;
  }
  
  /**
   * Validates that the supplied HTML does not contain any non-allowed HTML tags and cleans the supplied HTML from any
   * supplied attributes.
   * 
   * @param html
   *          the HTML to validate and clean
   * @return validated and cleaned HTML
   * @throws SignMessageContentException
   *           if the HTML contains illegal tags
   */
  private String validateAndCleanHtml(final String html) throws SignMessageContentException {

    // First validate the HTML based on allowed tags.
    // At this point we allow any attributes, but will fail on non-allowed
    // tags.
    //
    if (!Jsoup.isValid(html, this.htmlPreprocessingWhitelist)) {
      throw new SignMessageContentException("SignMessage HTML is not allowed - contains invalid HTML or non-allowed HTML tags");
    }

    // Then clean the HTML (removing attributes).
    //
    return Jsoup.clean(html, this.htmlWhitelist);
  }

  /**
   * Parses the supplied markdown into validated and cleaned HTML.
   * 
   * @param markdown
   *          the markdown text
   * @return validated and cleaned HTML
   * @throws SignMessageContentException
   *           if the generated HTML is illegal
   */
  private String markdownToHtml(String markdown) throws SignMessageContentException {

    // First translate the Markdown into HTML.
    //
    final Document document = this.markdownParser.parse(markdown);
    final String html = this.markdownHtmlRenderer.render(document);

    // Next, validate and clean the rendered HTML.
    //
    try {
      return this.validateAndCleanHtml(html);
    }
    catch (Exception e) {
      throw new SignMessageContentException("SignMessage markdown generated illegal HTML", e);
    }
  }  
  
  /**
   * A Jsoup {@link Whitelist} class that will allow all attributes. We use this instance when checking for invalid HTML
   * tags.
   */
  private static class PreprocessingWhitelist extends Whitelist {

    public PreprocessingWhitelist() {
      super();
    }

    @Override
    protected boolean isSafeAttribute(final String tagName, final Element el, final Attribute attr) {
      return true;
    }
  }  

}
