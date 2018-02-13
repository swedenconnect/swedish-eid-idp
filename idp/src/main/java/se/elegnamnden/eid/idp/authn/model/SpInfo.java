/*
 * Copyright 2016-2018 E-legitimationsnämnden
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
package se.elegnamnden.eid.idp.authn.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MVC model class for displaying information about a requesting Service Provider.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 * @author Stefan Santesson (stefan@aaa-sec.com)
 */
@Data
@NoArgsConstructor
@ToString
public class SpInfo {

  /** The SP display name. */
  private String displayName;

  /** The SP description. */
  private String description;

  /** The SP default logo - meaning that we return a logo that is not too small (16x16) and not too large (>100 px). */
  private String defaultLogoUrl;
}
