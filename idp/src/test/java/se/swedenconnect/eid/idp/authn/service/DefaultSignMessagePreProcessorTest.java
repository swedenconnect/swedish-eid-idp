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
package se.swedenconnect.eid.idp.authn.service;

import org.junit.Assert;
import org.junit.Test;

import se.litsec.shibboleth.idp.authn.service.SignMessageContentException;
import se.litsec.swedisheid.opensaml.saml2.signservice.dss.SignMessageMimeTypeEnum;
import se.swedenconnect.eid.idp.authn.service.DefaultSignMessagePreProcessor;

/**
 * Test cases for {@code DefaultSignMessagePreProcessor}.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class DefaultSignMessagePreProcessorTest {
  
  @Test
  public void testHtml() throws Exception {

    DefaultSignMessagePreProcessor processor = new DefaultSignMessagePreProcessor();

    String[][] tests = new String[][] {
        { "<p><b>Hej</b></p>", "<p><b>Hej</b></p>" },
        { "<p id=\"a\"><b>Hej</b></p>", "<p><b>Hej</b></p>" },
        { "<h1>Title</h1>", "<h1>Title</h1>" },
        { "<h1>Title</h1><blockquote>Hej</blockquote>", "<h1>Title</h1><blockquote>Hej</blockquote>" },
        { "<h1>Hej", "<h1>Hej</h1>" },
        { "<p><b>Martin", "<p><b>Martin</b></p>" },
        { "<table><tr><td>A</td>\n</tr></table>", "<table><tbody><tr><td>A</td>\n</tr></tbody></table>" },
        { "<div style=\"bla bla\">Hej</div>", "<div>Hej</div>" }
    };

    for (String[] t : tests) {
      String html = processor.processSignMessage(t[0], SignMessageMimeTypeEnum.TEXT_HTML);
      String expected = t[1].trim().replaceAll("\\s+", "").replaceAll("\\r|\\n", "");
      String actual = html.trim().replaceAll("\\s+", "").replaceAll("\\r|\\n", "");
      Assert.assertEquals(
        String.format("Expected input '%s' to be transformed to '%s'", t[0], t[1]), expected, actual);
    }
  }

  @Test
  public void testBadHtml() {

    DefaultSignMessagePreProcessor processor = new DefaultSignMessagePreProcessor();

    String[] tests = new String[] {
        "<body><p>hej</p></body>",
        "<html><body><p>hej</p></body></html>",
        "<script>a = b;</script>",
        "<<p>Hej</p>"
    };

    for (String t : tests) {
      try {
        processor.processSignMessage(t, SignMessageMimeTypeEnum.TEXT_HTML);
        Assert.fail("Expected SignMessageBadFormatException for input: " + t);
      }
      catch (SignMessageContentException e) {
      }
    }
  }
  
  @Test
  public void testMarkdown() throws Exception {

    DefaultSignMessagePreProcessor processor = new DefaultSignMessagePreProcessor();
     
    String[][] tests = new String[][] {
        { "This is *Sparta*", "<p>This is <em>Sparta</em></p>" },
        { "This is **Sparta**", "<p>This is <strong>Sparta</strong></p>" },
        { "## Martin\n\nHejsan", "<h2>Martin</h2> <p>Hejsan</p>" },
        { "| A | B | C |\n| --- | --- | --- |\n| Hej | Martin | Svejs |", "<table> <thead> <tr> <th>A</th> <th>B</th> <th>C</th> </tr> </thead> <tbody> <tr> <td>Hej</td> <td>Martin</td> <td>Svejs</td> </tr>  </tbody> </table>" },        
        { "\\[Martin\\]", "<p>[Martin]</p>" },
        { "\n\n## Svejs\nKalle", "<h2>Svejs</h2> <p>Kalle</p>" },
        { "<b>Bold text</b>\n\n## Heading", "<p><b>Bold text</b></p> <h2>Heading</h2>" }
    };

    for (String[] t : tests) {
      String html = processor.processSignMessage(t[0], SignMessageMimeTypeEnum.TEXT_MARKDOWN);
      String expected = t[1].trim().replaceAll("\\s+", " ");
      String actual = html.trim().replaceAll("\\r|\\n", " ").replaceAll("\\s+", " ");
      Assert.assertEquals(
        String.format("Expected input '%s' to be transformed to '%s'", t[0], t[1]), expected, actual);
    }
  }
  
  @Test
  public void testBadMarkdown() {

    DefaultSignMessagePreProcessor processor = new DefaultSignMessagePreProcessor();

    String[] tests = new String[] {
        "[Litsec](http://www.litsec.se)",
        "## Heading\n\n<script>a = b;</script>",
        "<body>Hello</body>"
    };

    for (String t : tests) {
      try {
        processor.processSignMessage(t, SignMessageMimeTypeEnum.TEXT_MARKDOWN);
        Assert.fail("Expected SignMessageBadFormatException for input: " + t);
      }
      catch (SignMessageContentException e) {
      }
    }
  }  

}
