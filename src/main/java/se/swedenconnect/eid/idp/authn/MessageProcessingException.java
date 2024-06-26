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

import java.io.Serial;

/**
 * Exception for error during processing of UI messages.
 *
 * @author Martin Lindström
 */
public class MessageProcessingException extends Exception {

  @Serial
  private static final long serialVersionUID = -8518564108321477660L;

  /**
   * Constructor.
   *
   * @param message the error message describing the exception
   */
  public MessageProcessingException(final String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message the error message describing the exception
   * @param cause the cause of the error
   */
  public MessageProcessingException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
