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
package se.swedenconnect.eid.idp.authn;

import org.springframework.stereotype.Component;
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
public class HtmlSignMessagePreProcessor extends AbstractMessagePreProcessor implements SignatureMessagePreprocessor {

  /**
   * Constructor.
   */
  public HtmlSignMessagePreProcessor() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public String processSignMessage(final String encodedText, final SignMessageMimeTypeEnum messageType)
      throws Saml2ErrorStatusException {

    try {
      final String input = this.decodeMessage(encodedText);

      // Text
      if (messageType == null || messageType == SignMessageMimeTypeEnum.TEXT) {
        return this.textToHtml(input);
      }
      // HTML
      else if (messageType == SignMessageMimeTypeEnum.TEXT_HTML) {
        return this.validateAndCleanHtml(input);
      }
      // Markdown
      else if (messageType == SignMessageMimeTypeEnum.TEXT_MARKDOWN) {
        return this.markdownToHtml(input);
      }
      else {
        throw new Saml2ErrorStatusException(Saml2ErrorStatus.SIGN_MESSAGE, "Unsupported SignMessage type");
      }
    }
    catch (final MessageProcessingException e) {
      throw new Saml2ErrorStatusException(Saml2ErrorStatus.SIGN_MESSAGE, e.getMessage(), e);
    }
  }

}
