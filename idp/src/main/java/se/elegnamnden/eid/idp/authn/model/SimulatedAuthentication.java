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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Class representing the result passed back from the simulated authentication view to the controller.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SimulatedAuthentication {
  
  /** The current authentication key. */
  private String authenticationKey;
  
  /** Service provider info. */
  private SpInfo spInfo;
  
  /** Is this a request for signature? */
  private boolean signature;
  
  /** The sign message to be displayed. */
  private SignMessageModel signMessage;
  
  /** Possible LoA:s to authenticate under. If only one exists this list is not assigned. */
  private List<String> possibleAuthnContextUris;
  
  /** The personalIdentityNumber for the selected user. */
  private String selectedUser;
  
  /** All attributes for the selected user. */
  private SimulatedUser selectedUserFull;
    
  /** The selected LoA. */
  private String selectedAuthnContextUri;
  
  /** Was the sign message displayed? */
  private boolean signMessageDisplayed;
  
}
