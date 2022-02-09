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
package se.swedenconnect.eid.idp.log.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * A controller serving the Shibboleth process log.
 * 
 * <p>
 * For development and test only!
 * </p>
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
@Controller
public class IdpLogController implements InitializingBean {

  /** Logging instance. */
  private Logger logger = LoggerFactory.getLogger(IdpLogController.class);

  /** Is log publishing enabled? */
  private boolean enabled = false;

  /** The path for the process log. */
  private String processLogPath;

  /** Message returned if the log publisher is not active. */
  private final String NOT_ENABLED = "Log publishing is not enabled";

  /**
   * Published the log file.
   * 
   * @param request
   *          the HTTP request
   * @return the bytes from the log file
   */
  @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public HttpEntity<byte[]> getProcessLog(HttpServletRequest request) {
    logger.debug("Request to download log data from {}", request.getRemoteAddr());

    if (!this.enabled) {
      return new HttpEntity<byte[]>(NOT_ENABLED.getBytes(Charset.defaultCharset()));
    }
    
    try {
      File file = new File(this.processLogPath);
      byte[] contents = Files.readAllBytes(file.toPath());
      return new HttpEntity<byte[]>(contents);
    }
    catch (Exception e) {
      return new HttpEntity<byte[]>(String.format("Failed to publish log - %s", e.getMessage()).getBytes(Charset.defaultCharset()));
    }
  }

  /**
   * Tells whether the log published is enabled or not.
   * 
   * @param enabled
   *          {@code true} for enabled and {@code false} for disabled
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Assigns the path to the Shibboleth process log.
   * 
   * @param processLogPath
   *          path
   */
  public void setProcessLogPath(String processLogPath) {
    this.processLogPath = processLogPath;
  }

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.enabled) {
      Assert.hasText(this.processLogPath, "The 'processLogPath' must be assigned");
    }
  }

}
