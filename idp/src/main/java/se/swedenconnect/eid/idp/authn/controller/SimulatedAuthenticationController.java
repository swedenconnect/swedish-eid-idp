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
package se.swedenconnect.eid.idp.authn.controller;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.CookieGenerator;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import se.litsec.shibboleth.idp.authn.ExternalAutenticationErrorCodeException;
import se.litsec.shibboleth.idp.authn.IdpErrorStatusException;
import se.litsec.shibboleth.idp.authn.context.HolderOfKeyContext;
import se.litsec.shibboleth.idp.authn.context.SignMessageContext;
import se.litsec.shibboleth.idp.authn.context.SignatureActivationDataContext;
import se.litsec.shibboleth.idp.authn.context.strategy.HolderOfKeyContextLookup;
import se.litsec.shibboleth.idp.authn.controller.AbstractExternalAuthenticationController;
import se.litsec.swedisheid.opensaml.saml2.attribute.AttributeConstants;
import se.litsec.swedisheid.opensaml.saml2.authentication.LevelofAssuranceAuthenticationContextURI;
import se.litsec.swedisheid.opensaml.saml2.authentication.psc.MatchValue;
import se.litsec.swedisheid.opensaml.saml2.authentication.psc.PrincipalSelection;
import se.litsec.swedisheid.opensaml.saml2.signservice.dss.SignMessageMimeTypeEnum;
import se.swedenconnect.eid.idp.authn.model.SignMessageModel;
import se.swedenconnect.eid.idp.authn.model.SimulatedAuthentication;
import se.swedenconnect.eid.idp.authn.model.SimulatedUser;
import se.swedenconnect.eid.idp.authn.model.SignMessageModel.DisplayType;

/**
 * A Spring MVC-controller for the IdP's authentication process. This is a simulated authentication where the user
 * simply selects which individual to authenticate as.
 *
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
@Controller
public class SimulatedAuthenticationController extends AbstractExternalAuthenticationController implements InitializingBean {

  /** Symbolic name for the action parameter value of "cancel". */
  public static final String ACTION_CANCEL = "cancel";

  /** Symbolic name for OK. */
  public static final String ACTION_OK = "ok";

  /** Maximum number of users to save in the above cookie. */
  public static final int MAX_SAVED_USERS = 20;

  /** Logging instance. */
  private final Logger logger = LoggerFactory.getLogger(SimulatedAuthenticationController.class);

  private static final String PNR_AUTHN = "simauth";
  // private static final String ORG_AUTHN = "orgauth";

  /** A list of simulated users that we offer as a choice when authenticating. */
  private List<SimulatedUser> staticUsers;

  /** The name of the authenticator. */
  private String authenticatorName;

  /** Fallback languages to be used in the currently selected language can not be used. */
  private List<String> fallbackLanguages;

  /** Helper bean for UI language select. */
  private UiLanguageHandler uiLanguageHandler;

  /** The cookie generator for selected user. */
  private CookieGenerator selectedUserCookieGenerator;

  /** The cookie generator for saved users. */
  private CookieGenerator savedUsersCookieGenerator;

  /** Strategy to find a HolderOfKeyContext. */
  private static HolderOfKeyContextLookup hokLookupStrategy = new HolderOfKeyContextLookup();

  /** {@inheritDoc} */
  @Override
  public String getAuthenticatorName() {
    return this.authenticatorName;
  }

  /** {@inheritDoc} */
  @Override
  protected ModelAndView doExternalAuthentication(
      final HttpServletRequest httpRequest,
      final HttpServletResponse httpResponse,
      final String key,
      final ProfileRequestContext<?, ?> profileRequestContext) throws ExternalAuthenticationException, IOException {

    // Is there an "auto authentication" cookie there?
    // This cookie enables automatic testing scenarios where no UI is displayed.
    //
    final AutoAuthnCookie autoAuthnCookie = Arrays.asList(httpRequest.getCookies())
      .stream()
      .filter(c -> c.getName().equals(AutoAuthnCookie.AUTO_AUTHN_COOKIE_NAME))
      .map(Cookie::getValue)
      .map(v -> AutoAuthnCookie.parse(v))
      .findFirst()
      .orElse(null);

    if (autoAuthnCookie != null) {
      final SignMessageContext signMessageContext =
          this.getSignSupportService().getSignMessageContext(profileRequestContext);

      final SimulatedAuthentication simResult = SimulatedAuthentication.builder()
        .authenticationKey(key)
        .selectedUser(autoAuthnCookie.getPersonalIdentityNumber())
        .signMessageDisplayed(signMessageContext != null && signMessageContext.isDoDisplayMessage())
        .build();

      return this.processAuthentication(httpRequest, httpResponse, ACTION_OK, simResult);
    }

    return this.startAuthentication(httpRequest, httpResponse, null);
  }

  /**
   * Start controller for the simulated authentication.
   *
   * @param httpRequest
   *          the HTTP request
   * @param httpResponse
   *          the HTTP response
   * @param language
   *          the language (optional)
   * @return a model and view object
   * @throws ExternalAuthenticationException
   *           for Shibboleth session errors
   * @throws IOException
   *           for IO errors
   */
  @RequestMapping(value = "/startAuth", method = RequestMethod.POST)
  public ModelAndView startAuthentication(
      final HttpServletRequest httpRequest,
      final HttpServletResponse httpResponse,
      @RequestParam(name = "language", required = false) final String language) throws ExternalAuthenticationException, IOException {

    if (language != null) {
      this.uiLanguageHandler.setUiLanguage(httpRequest, httpResponse, language);
    }

    final ProfileRequestContext<?, ?> context = this.getProfileRequestContext(httpRequest);

    try {
      final EntityDescriptor spMetadata = this.getPeerMetadata(context);

      final List<String> authnContextUris = this.getAuthnContextService().getPossibleAuthnContextClassRefs(context);

      final String view = PNR_AUTHN;
      // this.determineAuthenticationType(spMetadata, authnContextUris);

      final List<SimulatedUser> users = this.getStaticAndSavedUsers(httpRequest, view);

      // Figure out whether to display the normal authentication view or whether we should
      // display the view for organizational ID:s...
      //
      // TODO

      final ModelAndView modelAndView = new ModelAndView(view);
      modelAndView.addObject("uiLanguages", this.uiLanguageHandler.getUiLanguages());
      modelAndView.addObject("staticUsers", users);

      final SimulatedAuthentication simAuth = new SimulatedAuthentication();
      simAuth.setAuthenticationKey(this.getExternalAuthenticationKey(httpRequest));

      // Holder-of-key handling
      //
      boolean hokActive = false;
      final HolderOfKeyContext hokContext = hokLookupStrategy.apply(context);
      if (hokContext != null && hokContext.getClientCertificate() != null) {
        try {
          final SimulatedUser user = HolderOfKeySupport.parseCertificate(hokContext.getClientCertificate());
          users.add(user);
          simAuth.setSelectedUserFull(user);          
          simAuth.setSelectedUser(user.getPersonalIdentityNumber());
          simAuth.setFixedSelectedUser(true);
          
          simAuth.setSelectedAuthnContextUri(
            authnContextUris.stream()
              .filter(u -> LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_LOA4.equals(u))
              .findFirst()
              .orElse(null));
          httpRequest.getSession().setAttribute("hokUser", user);
          hokContext.setIssueHokAssertion(true);
          hokActive = true;
        }
        catch (final CertificateEncodingException e) {
          throw new IdpErrorStatusException(IdpErrorStatusException.getStatusBuilder(StatusCode.REQUESTER)
            .subStatusCode(StatusCode.AUTHN_FAILED)
            .statusMessage("Invalid user certificate")
            .build());
        }
      }

      // Check if the requester passed a PrincipalSelection extension. If so, we have a
      // pre-selected user ...
      //
      if (!hokActive) {
        final PrincipalSelection principalSelection = this.getPrincipalSelection(context);
        if (principalSelection != null) {
          final String preSelectedIdentityNumber = principalSelection.getMatchValues()
            .stream()
            .filter(mv -> AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER.equals(mv.getName()))
            .map(MatchValue::getValue)
            .findFirst()
            .orElse(null);
          if (preSelectedIdentityNumber != null) {
            // Check that it is a valid user ...
            if (!users.stream().filter(u -> preSelectedIdentityNumber.equals(u.getPersonalIdentityNumber())).findFirst().isPresent()) {
              logger.info("AuthnRequest contained PrincipalSelection for user '{}' - this user is unknown - ignoring",
                preSelectedIdentityNumber);
            }
            else {
              if (PersonalIdentityNumberSupport.validForSP(preSelectedIdentityNumber, spMetadata, null)) {
                logger.debug("User '{}' was pre-selected (PrincipalSelection extension)", preSelectedIdentityNumber);
                simAuth.setSelectedUser(preSelectedIdentityNumber);
                simAuth.setFixedSelectedUser(true);
              }
              else {
                logger.info("AuthnRequest contained PrincipalSelection for user '{}' - this ID is a coordination number "
                    + "and the SP does not accept coordination numbers - ignoring",
                  preSelectedIdentityNumber);
              }
            }
          }
        }
      }

      // Check if we already have a selected user (from previous sessions).
      //
      if (simAuth.getSelectedUser() == null) {
        final SimulatedUser selectedUser = this.getSavedSelectedUser(httpRequest, users);
        if (selectedUser != null
            && PersonalIdentityNumberSupport.validForSP(selectedUser.getPersonalIdentityNumber(), spMetadata, null)) {
          simAuth.setSelectedUser(selectedUser.getPersonalIdentityNumber());
          // simAuth.setSelectedUserFull(selectedUser);
        }
      }

      // Assign the SP info ...
      //
      simAuth.setSpInfo(SpInfoHandler.buildSpInfo(spMetadata, LocaleContextHolder.getLocale().getLanguage(),
        this.fallbackLanguages));

      // Sign message support ...
      //
      if (this.getSignSupportService().isSignatureServicePeer(context)) {

        simAuth.setSignature(true);

        final SignMessageContext signMessageContext = this.getSignSupportService().getSignMessageContext(context);
        if (signMessageContext != null && signMessageContext.isDoDisplayMessage()) {
          final SignMessageModel signMessageModel = new SignMessageModel();
          signMessageModel.setHtml(signMessageContext.getMessageToDisplay());
          signMessageModel.setDisplayType(signMessageContext.getMimeType() == SignMessageMimeTypeEnum.TEXT ? DisplayType.MONOSPACE_TEXT
              : DisplayType.HTML);
          simAuth.setSignMessage(signMessageModel);
        }
      }
      else {
        simAuth.setSignature(false);
      }

      // Set the possible authentication context URI:s that the user can authenticate under ...
      //
      if (simAuth.getSelectedAuthnContextUri() == null) {
        simAuth.setSelectedAuthnContextUri(authnContextUris.get(0));
        if (authnContextUris.size() > 1) {
          simAuth.setPossibleAuthnContextUris(authnContextUris);
        }
      }

      modelAndView.addObject("simulatedAuthentication", simAuth);

      return modelAndView;
    }
    catch (final ExternalAutenticationErrorCodeException e) {
      this.error(httpRequest, httpResponse, e);
      return null;
    }
  }

  // private String determineAuthenticationType(final EntityDescriptor spMetadata, final List<String> authnContextUris)
  // {
  // final List<String> entityCategories = EntityCategoryMetadataHelper.getEntityCategories(spMetadata);
  // final boolean loa2_orgid =
  // entityCategories.contains(EntityCategoryConstants.SERVICE_ENTITY_CATEGORY_LOA2_ORGID.getUri());
  // final boolean loa3_orgid =
  // entityCategories.contains(EntityCategoryConstants.SERVICE_ENTITY_CATEGORY_LOA3_ORGID.getUri());
  // if (!loa2_orgid && !loa3_orgid) {
  // return PNR_AUTHN;
  // }
  // for (final String loa : authnContextUris) {
  // if (!loa2_orgid) {
  // authnContextUris.removeAll(Arrays.asList(LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_LOA2,
  // LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_LOA2_NONRESIDENT,
  // LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_UNCERTIFIED_LOA2));
  // }
  // if (!loa3_orgid) {
  // authnContextUris.removeAll(Arrays.asList(LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_LOA3,
  // LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_LOA3_NONRESIDENT,
  // LevelofAssuranceAuthenticationContextURI.AUTHN_CONTEXT_URI_UNCERTIFIED_LOA3));
  // }
  // }
  // return ORG_AUTHN;
  // }

  /**
   * Processes the result sent in from the view.
   *
   * @param httpRequest
   *          the HTTP request
   * @param httpResponse
   *          the HTTP response
   * @param action
   *          the result from the action (ok or cancel)
   * @param result
   *          the "authentication result"
   * @return a {@code ModelAndView} if control is to return to the view or {@code null} to terminate the operation
   * @throws ExternalAuthenticationException
   *           for Shibboleth session errors
   * @throws IOException
   *           for IO errors
   */
  @RequestMapping(value = "/simulatedAuth", method = RequestMethod.POST)
  public ModelAndView processAuthentication(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
      @RequestParam("action") final String action,
      @ModelAttribute("authenticationResult") final SimulatedAuthentication result) throws ExternalAuthenticationException, IOException {

    if ("cancel".equals(action)) {
      this.cancel(httpRequest, httpResponse);
      return null;
    }

    if ("NONE".equals(result.getSelectedUser())) {
      logger.debug("Binding errors, returning control to the view");
      return this.doExternalAuthentication(httpRequest, httpResponse, result.getAuthenticationKey(), this.getProfileRequestContext(
        httpRequest));
    }

    final SimulatedUser user = this.processSelectedUser(httpRequest, httpResponse, result);

    if (user == null) {
      this.error(httpRequest, httpResponse, AuthnEventIds.INVALID_AUTHN_CTX);
      return null;
    }
    logger.debug("Authenticated user: {}", user);

    try {
      final ProfileRequestContext<?, ?> context = this.getProfileRequestContext(httpRequest);

      // Which authentication context URI should be included in the assertion?
      //
      final String authnContextClassRef = this.getAuthnContextService()
        .getReturnAuthnContextClassRef(context, result.getSelectedAuthnContextUri());

      // Issue attributes ...
      //
      final List<Attribute> attributes = new ArrayList<>();
      attributes.add(
        AttributeConstants.ATTRIBUTE_TEMPLATE_PERSONAL_IDENTITY_NUMBER.createBuilder().value(user.getPersonalIdentityNumber()).build());
      attributes.add(
        AttributeConstants.ATTRIBUTE_TEMPLATE_GIVEN_NAME.createBuilder().value(user.getGivenName()).build());
      attributes.add(
        AttributeConstants.ATTRIBUTE_TEMPLATE_SN.createBuilder().value(user.getSurname()).build());
      attributes.add(
        AttributeConstants.ATTRIBUTE_TEMPLATE_DISPLAY_NAME.createBuilder().value(user.getDisplayName()).build());
      attributes.add(
        AttributeConstants.ATTRIBUTE_TEMPLATE_DATE_OF_BIRTH.createBuilder().value(
          PersonalIdentityNumberSupport.getBirthDate(user.getPersonalIdentityNumber())).build());

      // Issue signMessageDigest if we displayed the sign message.
      //
      if (result.isSignMessageDisplayed()) {
        attributes.add(this.createSignMessageDigestAttribute(
          context, this.getSignSupportService().getSignMessageContext(context).getMessage()));
      }

      // Check if we should issue a SAD attribute.
      //
      final SignatureActivationDataContext sadContext = this.getSignSupportService().getSadContext(context);
      if (sadContext != null && result.isSignMessageDisplayed() && this.getSignSupportService().isSignatureServicePeer(context)) {
        final String sad = this.getSignSupportService()
          .issueSAD(
            context, attributes, AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER, authnContextClassRef);
        attributes.add(AttributeConstants.ATTRIBUTE_TEMPLATE_SAD.createBuilder().value(sad).build());
      }

      this.success(httpRequest, httpResponse, user.getPersonalIdentityNumber(), attributes, authnContextClassRef, null, null);
      return null;
    }
    catch (final ExternalAutenticationErrorCodeException e) {
      this.error(httpRequest, httpResponse, e);
      return null;
    }
  }

  /**
   * Helper method that is invoked when a user has been selected. It will save the selected user in a cookie for future
   * use.
   *
   * @param httpRequest
   *          the HTTP request
   * @param httpResponse
   *          the HTTP response
   * @param result
   *          the result from the view
   * @return the selected user or {@code null}
   */
  private SimulatedUser processSelectedUser(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
      final SimulatedAuthentication result) {

    if (result.getSelectedUser() == null && result.getSelectedUserFull() == null) {
      // Should never happen ...
      logger.error("No user selected");
      return null;
    }
    SimulatedUser user = null;
        
    if (result.getSelectedUser() != null) {
      
      // Before looking among the static users, check if it is a certificate user.
      final SimulatedUser hokUser = (SimulatedUser) httpRequest.getSession().getAttribute("hokUser");
      if (hokUser != null && result.getSelectedUser().equals(hokUser.getPersonalIdentityNumber())) {
        httpRequest.getSession().removeAttribute("hokUser");
        return hokUser;
      }
      
      // Find the given name and surname. First check the static users ...
      user = this.staticUsers.stream()
        .filter(u -> result.getSelectedUser().equals(u.getPersonalIdentityNumber()))
        .findFirst()
        .orElse(null);

      if (user == null) {
        // Check if we find the name among the user-saved names ...
        final List<SimulatedUser> savedUsers = this.getSavedUsers(httpRequest);
        user = savedUsers.stream()
          .filter(u -> result.getSelectedUser().equals(u.getPersonalIdentityNumber()))
          .findFirst()
          .orElse(null);
      }
    }
    else {
      user = result.getSelectedUserFull();
      final String pnr = user.getPersonalIdentityNumber();

      // Do we have to update the saved users? It may also be a user from the static users.
      final boolean staticUser = this.staticUsers.stream()
        .filter(u -> u.getPersonalIdentityNumber().equals(pnr))
        .findFirst()
        .isPresent();

      if (!staticUser) {
        final List<SimulatedUser> savedUsers = this.getSavedUsers(httpRequest);

        final boolean completeMatch = savedUsers.stream()
          .filter(u -> result.getSelectedUserFull().equals(u))
          .findFirst()
          .isPresent();
        if (!completeMatch) {
          final List<SimulatedUser> newSavedUsers = new ArrayList<>();
          newSavedUsers.add(user);
          for (final SimulatedUser s : savedUsers) {
            if (newSavedUsers.size() >= MAX_SAVED_USERS) {
              break;
            }
            if (s.getPersonalIdentityNumber().equals(pnr)) {
              // The personal id number existed (but not the same name). Skip this.
              continue;
            }
            newSavedUsers.add(s);
          }

          // Update the cookie ...
          this.savedUsersCookieGenerator.addCookie(httpResponse, SimulatedUser.encodeList(newSavedUsers));
        }
      }
    }

    if (user == null) {
      // Should never happen ...
      logger.error("No user selected");
      return null;
    }

    // Save the selected user in a cookie (for pre-selection the next time).
    //
    this.selectedUserCookieGenerator.addCookie(httpResponse, user.getPersonalIdentityNumber());

    return user;
  }

  /**
   * Returns the simulated user that was saved since a previous session.
   *
   * @param request
   *          the HTTP servlet request
   * @param users
   *          the simulated users available
   * @return the user or {@code null} if no cookie is available
   */
  private SimulatedUser getSavedSelectedUser(final HttpServletRequest request, final List<SimulatedUser> users) {
    final String id = Arrays.asList(request.getCookies())
      .stream()
      .filter(c -> c.getName().equals(this.selectedUserCookieGenerator.getCookieName()))
      .map(c -> c.getValue())
      .findFirst()
      .orElse(null);

    if (id != null) {
      return users.stream()
        .filter(u -> u.getPersonalIdentityNumber().equals(id))
        .findFirst()
        .orElse(null);
    }
    else {
      return null;
    }
  }

  /**
   * Returns a list of the users that have been entered in the "Advanced" view.
   *
   * @param request
   *          the HTTP request
   * @return a list of simulated users
   */
  private List<SimulatedUser> getSavedUsers(final HttpServletRequest request) {
    final String v = Arrays.asList(request.getCookies())
      .stream()
      .filter(c -> c.getName().equals(this.savedUsersCookieGenerator.getCookieName()))
      .map(c -> c.getValue())
      .findFirst()
      .orElse(null);

    return v != null ? SimulatedUser.parseList(v) : Collections.emptyList();
  }

  /**
   * Returns a list of the users that have been entered in the "Advanced" view and the static users.
   *
   * @param request
   *          the HTTP request
   * @param authnType
   *          ordinary or org authn?
   * @return a list of simulated users
   */
  private List<SimulatedUser> getStaticAndSavedUsers(final HttpServletRequest request, final String authnType) {

    // TODO: filter users

    final List<SimulatedUser> users = new ArrayList<>(this.staticUsers.size());
    users.addAll(this.getSavedUsers(request));
    users.addAll(this.staticUsers);
    Collections.sort(users);
    return users;
  }

  /**
   * Assigns the simulated users that should be possible to select in the UI.
   *
   * @param staticUsers
   *          a list of static simulated users
   */
  public void setStaticUsers(final List<SimulatedUser> staticUsers) {
    this.staticUsers = staticUsers;
  }

  /**
   * Assigns the name of this authenticator.
   *
   * @param authenticatorName
   *          the name
   */
  public void setAuthenticatorName(final String authenticatorName) {
    this.authenticatorName = authenticatorName;
  }

  /**
   * Assigns the fallback languages to be used in the currently selected language can not be used.
   *
   * @param fallbackLanguages
   *          a list of country codes
   */
  public void setFallbackLanguages(final List<String> fallbackLanguages) {
    this.fallbackLanguages = fallbackLanguages;
  }

  /**
   * Assigns the helper bean for handling user UI language.
   *
   * @param uiLanguageHandler
   *          the UI language handler
   */
  public void setUiLanguageHandler(final UiLanguageHandler uiLanguageHandler) {
    this.uiLanguageHandler = uiLanguageHandler;
  }

  /**
   * Assigns the cookie generator for the selected user cookie.
   *
   * @param selectedUserCookieGenerator
   *          cookie generator
   */
  public void setSelectedUserCookieGenerator(final CookieGenerator selectedUserCookieGenerator) {
    this.selectedUserCookieGenerator = selectedUserCookieGenerator;
  }

  /**
   * Assigns the cookie generator for the saved users cookie.
   *
   * @param savedUsersCookieGenerator
   *          cookie generator
   */
  public void setSavedUsersCookieGenerator(final CookieGenerator savedUsersCookieGenerator) {
    this.savedUsersCookieGenerator = savedUsersCookieGenerator;
  }

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    Assert.notEmpty(this.staticUsers, "The property 'staticUsers' must be assigned");
    Assert.hasText(this.authenticatorName, "The property 'authenticatorName' must be assigned");
    Assert.notNull(this.fallbackLanguages, "The property 'fallbackLanguages' must be assigned");
    Assert.notNull(this.uiLanguageHandler, "The property 'uiLanguageHandler' must be assigned");
    Assert.notNull(this.selectedUserCookieGenerator, "The property 'selectedUserCookieGenerator' must be assigned");
    Assert.notNull(this.savedUsersCookieGenerator, "The property 'savedUsersCookieGenerator' must be assigned");
  }

}
