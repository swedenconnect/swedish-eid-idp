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
package se.swedenconnect.eid.idp.users;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * User configuration properties.
 *
 * @author Martin Lindström
 */
@ConfigurationProperties(prefix = "users")
public class UsersConfigurationProperties extends ArrayList<SimulatedUser> {

  @Serial
  private static final long serialVersionUID = 6621423400335019153L;

  public List<SimulatedUser> getUsers() {
    return this;
  }

}
