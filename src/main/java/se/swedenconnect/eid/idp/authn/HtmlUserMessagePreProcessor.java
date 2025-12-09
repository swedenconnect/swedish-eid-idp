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

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatus;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;
import se.swedenconnect.spring.saml.idp.extensions.UserMessagePreprocessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Transforms all user messages to safe HTML for later inclusion in the IdP UI.
 *
 * @author Martin Lindström
 */
@Component
public class HtmlUserMessagePreProcessor extends AbstractMessagePreProcessor implements UserMessagePreprocessor {

  private static final MimeType TEXT_MARKDOWN = MimeTypeUtils.parseMimeType("text/markdown");

  /** {@inheritDoc} */
  @NonNull
  @Override
  public Map<String, String> processUserMessage(@NonNull final Map<String, String> messages,
      @NonNull final MimeType mimeType) throws Saml2ErrorStatusException {

    try {
      final Map<String, String> processed = new HashMap<>();

      if (MimeTypeUtils.TEXT_PLAIN.equals(mimeType)) {
        for (final Map.Entry<String, String> entry : messages.entrySet()) {
          processed.put(entry.getKey(), this.textToHtml(this.decodeMessage(entry.getValue())));
        }
      }
      else if (TEXT_MARKDOWN.equals(mimeType)) {
        for (final Map.Entry<String, String> entry : messages.entrySet()) {
          processed.put(entry.getKey(), this.markdownToHtml(this.decodeMessage(entry.getValue())));
        }
      }
      else {
        throw new Saml2ErrorStatusException(Saml2ErrorStatus.INVALID_USER_MESSAGE, "Unsupported MIME type");
      }
      return processed;
    }
    catch (final MessageProcessingException e) {
      throw new Saml2ErrorStatusException(Saml2ErrorStatus.INVALID_USER_MESSAGE, e.getMessage(), e);
    }
  }

}
