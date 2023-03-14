/*
 * Copyright 2023 Sweden Connect
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

/**
 * An implementation of {@link UserDetailsManager} that handles the simulated users.
 *
 * @author Martin Lindström
 */
public class SimulatedUserDetailsManager implements UserDetailsManager {

  /** The simulated users. */
  private final Map<String, SimulatedUser> users = new HashMap<>();

  /**
   * Returns all users as a list.
   * 
   * @return all users
   */
  public List<SimulatedUser> getUsers() {
    return this.users.values().stream().toList();
  }

  /** {@inheritDoc} */
  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    return Optional.ofNullable(this.users.get(username))
        .orElseThrow(() -> new UsernameNotFoundException(username));
  }

  /** {@inheritDoc} */
  @Override
  public void createUser(final UserDetails user) {
    if (!SimulatedUser.class.isInstance(user)) {
      throw new IllegalArgumentException("Expected " + SimulatedUser.class.getSimpleName());
    }
    this.users.put(user.getUsername(), SimulatedUser.class.cast(user));
  }

  /** {@inheritDoc} */
  @Override
  public void updateUser(final UserDetails user) {
    if (!this.userExists(user.getUsername())) {
      throw new IllegalArgumentException("User does not exist");
    }
    this.createUser(user);
  }

  /** {@inheritDoc} */
  @Override
  public void deleteUser(final String username) {
    this.users.remove(username);
  }

  /** {@inheritDoc} */
  @Override
  public void changePassword(final String oldPassword, final String newPassword) {
  }

  /** {@inheritDoc} */
  @Override
  public boolean userExists(final String username) {
    return this.users.containsKey(username);
  }

}
