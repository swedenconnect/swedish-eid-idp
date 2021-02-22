/*
 * Copyright 2016-2021 Sweden Connect
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
package se.elegnamnden.eid.idp.authn.service;

import java.util.Optional;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.authn.AuthnEventIds;
import se.litsec.shibboleth.idp.authn.ExternalAutenticationErrorCodeException;
import se.litsec.shibboleth.idp.authn.context.AuthnContextClassContext;
import se.litsec.shibboleth.idp.authn.service.impl.AuthnContextServiceImpl;
import se.litsec.swedisheid.opensaml.saml2.authentication.LevelofAssuranceAuthenticationContextURI.LoaEnum;
import se.litsec.swedisheid.opensaml.saml2.metadata.entitycategory.EntityCategoryConstants;
import se.litsec.swedisheid.opensaml.saml2.metadata.entitycategory.EntityCategoryMetadataHelper;

/**
 * Extends the default {@code AuthnContextService} behaviour with checks for delivering to an eIDAS proxy service.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class ExtendedAuthnContextServiceImpl extends AuthnContextServiceImpl {

  /** Logging instance. */
  private final Logger log = LoggerFactory.getLogger(ExtendedAuthnContextServiceImpl.class);

  /**
   * Extends the base functionality with checks to ensure that only an eIDAS Proxy Service may
   * authenticate according to the eIDAS LoA:s.
   */
  @Override
  public void processRequest(final ProfileRequestContext context) throws ExternalAutenticationErrorCodeException {
    super.processRequest(context);
    
    if (!this.isEidasProxyServicePeer(context)) {
      final String logId = this.getLogString(context);

      AuthnContextClassContext authnContextContext = this.getAuthnContextClassContext(context);
      for (String uri : authnContextContext.getAuthnContextClassRefs()) {
        if (this.isEidasURI(uri)) {
          log.info("Requested AuthnContext URI '{}' is not supported since the SP is not an eIDAS Proxy Service, ignoring [{}]", uri, logId);
          authnContextContext.deleteAuthnContextClassRef(uri);
        }
      }
      
      if (authnContextContext.isEmpty()) {
        final String msg = "No valid AuthnContext URI:s were specified in AuthnRequest";
        log.info("{} - can not proceed [{}]", msg, logId);
        throw new ExternalAutenticationErrorCodeException(AuthnEventIds.REQUEST_UNSUPPORTED, msg);
      }
    }
  }
  
  /**
   * Predicate that tells if the supplied URI is a URI indicating an eIDAS Loa URI.
   * 
   * @param uri
   *          the URI to test
   * @return true if the supplied URI is for eIDAS, and false otherwise
   */
  protected boolean isEidasURI(final String uri) {
    return Optional.ofNullable(LoaEnum.parse(uri)).map(LoaEnum::isEidasUri).orElse(false);
  }

  /**
   * Predicate telling if the peer is an eIDAS proxy service.
   * 
   * @param context
   *          the profile context
   * @return true if the peer is an eIDAS proxy service and false otherwise
   */
  protected boolean isEidasProxyServicePeer(final ProfileRequestContext context) {
    EntityDescriptor peerMetadata = this.getPeerMetadata(context);
    if (peerMetadata == null) {
      log.error("No metadata available for connecting SP");
      return false;
    }
    return EntityCategoryMetadataHelper.getEntityCategories(peerMetadata)
      .stream()
      .filter(c -> EntityCategoryConstants.SERVICE_ENTITY_CATEGORY_EIDAS_PNR_DELIVERY.getUri().equals(c))
      .findFirst()
      .isPresent();
  }  
  
}
