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

import org.junit.jupiter.api.Test;

/**
 * Test cases for AbstractMessagePreProcessorTest.
 * @author Martin Lindström
 */
public class AbstractMessagePreProcessorTest {

  @Test
  public void testSimpleMarkdown() throws MessageProcessingException {
    final String markdown = "### Heading\n\nThis is a **bold** text";
    final String html = new TestMessagePreProcessor().convertMarkdown(markdown);
    System.out.println(html);
  }

  public static class TestMessagePreProcessor extends AbstractMessagePreProcessor {

    public String convertMarkdown(final String markdown) throws MessageProcessingException {
      return this.markdownToHtml(markdown);
    }

  }
}
