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
package se.swedenconnect.eid.idp.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for our simulated IdP.
 * 
 * @author Martin Lindström
 */
@ConfigurationProperties("authn")
@Data
public class IdpConfigurationProperties {
  
  /**
   * The name of the authentication provider.
   */
  private String providerName;
  
  /** 
   * The authentication path. Where the Spring Security flow directs the user for authentication by our implementation. 
   */
  private String authnPath;
  
  /** 
   * The resume path. Where we redirect back the user after that we are done.  
   */
  private String resumePath;
  
  /**
   * The supported LoA:s.
   */
  private List<String> supportedLoas;
  
  /**
   * The SAML entity categories this IdP declares.
   */
  private List<String> entityCategories;

}
