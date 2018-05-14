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
package se.elegnamnden.eid.idp.authn.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Attribute;
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

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import se.elegnamnden.eid.idp.authn.model.SignMessageModel;
import se.elegnamnden.eid.idp.authn.model.SignMessageModel.DisplayType;
import se.elegnamnden.eid.idp.authn.model.SimulatedAuthentication;
import se.elegnamnden.eid.idp.authn.model.SimulatedUser;
import se.litsec.shibboleth.idp.authn.ExternalAutenticationErrorCodeException;
import se.litsec.shibboleth.idp.authn.context.SignMessageContext;
import se.litsec.shibboleth.idp.authn.context.SignatureActivationDataContext;
import se.litsec.shibboleth.idp.authn.controller.AbstractExternalAuthenticationController;
import se.litsec.swedisheid.opensaml.saml2.attribute.AttributeConstants;
import se.litsec.swedisheid.opensaml.saml2.signservice.dss.SignMessageMimeTypeEnum;

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

  /** The name of the selected-user cookie. */
  public static final String SELECTED_USER_COOKIE_NAME = "selectedUser";

  /** Logging instance. */
  private final Logger logger = LoggerFactory.getLogger(SimulatedAuthenticationController.class);

  /** A list of simulated users that we offer as a choice when authenticating. */
  private List<SimulatedUser> staticUsers;

  /** The name of the authenticator. */
  private String authenticatorName;

  /** Fallback languages to be used in the currently selected language can not be used. */
  private List<String> fallbackLanguages;

  /** Helper bean for UI language select. */
  private UiLanguageHandler uiLanguageHandler;

  /** {@inheritDoc} */
  @Override
  public String getAuthenticatorName() {
    return this.authenticatorName;
  }

  /** {@inheritDoc} */
  @Override
  protected ModelAndView doExternalAuthentication(
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse,
      String key,
      ProfileRequestContext<?, ?> profileRequestContext) throws ExternalAuthenticationException, IOException {

    // Is there an "auto authentication" cookie there?
    // This cookie enables automatic testing scenarios where no UI is displayed.
    //
    AutoAuthnCookie autoAuthnCookie = Arrays.asList(httpRequest.getCookies())
      .stream()
      .filter(c -> c.getName().equals(AutoAuthnCookie.AUTO_AUTHN_COOKIE_NAME))
      .map(Cookie::getValue)
      .map(v -> AutoAuthnCookie.parse(v))
      .findFirst()
      .orElse(null);

    if (autoAuthnCookie != null) {
      SimulatedAuthentication simResult = SimulatedAuthentication.builder()
        .authenticationKey(key)
        .selectedUser(autoAuthnCookie.getPersonalIdentityNumber())
        .signMessageDisplayed(true)
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
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse,
      @RequestParam(name = "language", required = false) String language) throws ExternalAuthenticationException, IOException {

    if (language != null) {
      this.uiLanguageHandler.setUiLanguage(httpRequest, httpResponse, language);
    }

    final ProfileRequestContext<?, ?> context = this.getProfileRequestContext(httpRequest);

    try {
      ModelAndView modelAndView = new ModelAndView("simauth");
      modelAndView.addObject("uiLanguages", this.uiLanguageHandler.getUiLanguages());
      modelAndView.addObject("staticUsers", this.getStaticUsers());

      SimulatedAuthentication simAuth = new SimulatedAuthentication();
      simAuth.setAuthenticationKey(this.getExternalAuthenticationKey(httpRequest));

      // Check if we already have a selected user (from previous sessions).
      //
      SimulatedUser selectedUser = this.getSelectedUser(httpRequest);
      if (selectedUser != null) {
        simAuth.setSelectedUser(selectedUser.getPersonalIdentityNumber());
        simAuth.setSelectedUserFull(selectedUser);
      }

      // Assign the SP info ...
      //
      simAuth.setSpInfo(SpInfoHandler.buildSpInfo(this.getPeerMetadata(context), LocaleContextHolder.getLocale().getLanguage(),
        this.fallbackLanguages));

      // Sign message support ...
      //
      boolean willDisplaySignMessage = false;

      if (this.getSignSupportService().isSignatureServicePeer(context)) {

        simAuth.setSignature(true);

        SignMessageContext signMessageContext = this.getSignSupportService().getSignMessageContext(context);
        if (signMessageContext != null && signMessageContext.isDoDisplayMessage()) {
          SignMessageModel signMessageModel = new SignMessageModel();
          signMessageModel.setHtml(signMessageContext.getMessageToDisplay());
          signMessageModel.setDisplayType(signMessageContext.getMimeType() == SignMessageMimeTypeEnum.TEXT ? DisplayType.MONOSPACE_TEXT
              : DisplayType.HTML);
          simAuth.setSignMessage(signMessageModel);
          willDisplaySignMessage = true;
        }
      }
      else {
        simAuth.setSignature(false);
      }

      // Get hold of the possible authentication context URI:s that the user can authenticate under ...
      //
      List<String> authnContextUris = this.getAuthnContextService().getPossibleAuthnContextClassRefs(context, willDisplaySignMessage);
      simAuth.setSelectedAuthnContextUri(authnContextUris.get(0));
      if (authnContextUris.size() > 1) {
        simAuth.setPossibleAuthnContextUris(authnContextUris);
      }

      modelAndView.addObject("simulatedAuthentication", simAuth);

      return modelAndView;
    }
    catch (ExternalAutenticationErrorCodeException e) {
      this.error(httpRequest, httpResponse, e);
      return null;
    }
  }

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
  public ModelAndView processAuthentication(HttpServletRequest httpRequest,
      HttpServletResponse httpResponse,
      @RequestParam("action") String action,
      @ModelAttribute("authenticationResult") SimulatedAuthentication result) throws ExternalAuthenticationException, IOException {

    if ("cancel".equals(action)) {
      this.cancel(httpRequest, httpResponse);
      return null;
    }

    if ("NONE".equals(result.getSelectedUser())) {
      logger.debug("Binding errors, returning control to the view");
      return this.doExternalAuthentication(httpRequest, httpResponse, result.getAuthenticationKey(), this.getProfileRequestContext(
        httpRequest));
    }

    if (result.getSelectedUser() == null) {
      logger.error("No user selected");
      this.error(httpRequest, httpResponse, AuthnEventIds.INVALID_AUTHN_CTX);
      return null;
    }

    SimulatedUser user = this.staticUsers.stream()
      .filter(u -> result.getSelectedUser().equals(u.getPersonalIdentityNumber()))
      .findFirst()
      .orElse(null);

    if (user == null) {
      logger.error("No user selected");
      this.error(httpRequest, httpResponse, AuthnEventIds.INVALID_AUTHN_CTX);
      return null;
    }

    logger.debug("Authenticated user: {}", user);

    // Save the selected user in a cookie (for pre-selection the next time).
    //
    this.saveSelectedUser(user.getPersonalIdentityNumber(), httpResponse);

    try {
      final ProfileRequestContext<?, ?> context = this.getProfileRequestContext(httpRequest);

      // Which authentication context URI should be included in the assertion?
      //
      String authnContextClassRef = this.getAuthnContextService().getReturnAuthnContextClassRef(
        context, result.getSelectedAuthnContextUri(), result.isSignMessageDisplayed());

      // Issue attributes ...
      //
      List<Attribute> attributes = new ArrayList<>();

      attributes.add(
        AttributeConstants.ATTRIBUTE_TEMPLATE_PERSONAL_IDENTITY_NUMBER.createBuilder().value(user.getPersonalIdentityNumber()).build());
      attributes.add(
        AttributeConstants.ATTRIBUTE_TEMPLATE_GIVEN_NAME.createBuilder().value(user.getGivenName()).build());
      attributes.add(
        AttributeConstants.ATTRIBUTE_TEMPLATE_SN.createBuilder().value(user.getSurname()).build());
      attributes.add(
        AttributeConstants.ATTRIBUTE_TEMPLATE_DISPLAY_NAME.createBuilder().value(user.getDisplayName()).build());

      // Check if we should issue a SAD attribute.
      //
      SignatureActivationDataContext sadContext = this.getSignSupportService().getSadContext(context);
      if (sadContext != null && result.isSignMessageDisplayed() && this.getSignSupportService().isSignatureServicePeer(context)) {
        String sad = this.getSignSupportService().issueSAD(
          context, attributes, AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER, authnContextClassRef);
        attributes.add(AttributeConstants.ATTRIBUTE_TEMPLATE_SAD.createBuilder().value(sad).build());
      }

      this.success(httpRequest, httpResponse, user.getPersonalIdentityNumber(), attributes, authnContextClassRef, null, null);
      return null;
    }
    catch (ExternalAutenticationErrorCodeException e) {
      this.error(httpRequest, httpResponse, e);
      return null;
    }
  }

  /**
   * Saves the user id of the selected test user.
   * 
   * @param selectedUser
   *          the id of the selected user
   * @param response
   *          the HTTP servlet response
   */
  private void saveSelectedUser(String selectedUser, HttpServletResponse response) {
    Cookie cookie = new Cookie(SELECTED_USER_COOKIE_NAME, selectedUser);
    cookie.setPath("/idp");
    response.addCookie(cookie);
  }

  /**
   * Returns the simulated user that was saved since a previous session.
   * 
   * @param request
   *          the HTTP servlet request
   * @return the user or {@code null} if no cookie is available
   */
  private SimulatedUser getSelectedUser(HttpServletRequest request) {
    String id = Arrays.asList(request.getCookies())
      .stream()
      .filter(c -> c.getName().equals(SELECTED_USER_COOKIE_NAME))
      .map(c -> c.getValue())
      .findFirst()
      .orElse(null);

    if (id != null) {
      return this.staticUsers.stream()
        .filter(u -> u.getPersonalIdentityNumber().equals(id))
        .findFirst()
        .orElse(null);
    }
    else {
      return null;
    }
  }

  /**
   * Returns the list of static users.
   * 
   * @return the static list of simulated users.
   */
  private List<SimulatedUser> getStaticUsers() {
    return this.staticUsers;
  }

  /**
   * Assigns the simulated users that should be possible to select in the UI.
   * 
   * @param staticUsers
   *          a list of static simulated users
   */
  public void setStaticUsers(List<SimulatedUser> staticUsers) {
    this.staticUsers = staticUsers;
  }

  /**
   * Assigns the name of this authenticator.
   * 
   * @param authenticatorName
   *          the name
   */
  public void setAuthenticatorName(String authenticatorName) {
    this.authenticatorName = authenticatorName;
  }

  /**
   * Assigns the fallback languages to be used in the currently selected language can not be used.
   * 
   * @param fallbackLanguages
   *          a list of country codes
   */
  public void setFallbackLanguages(List<String> fallbackLanguages) {
    this.fallbackLanguages = fallbackLanguages;
  }

  /**
   * Assigns the helper bean for handling user UI language.
   * 
   * @param uiLanguageHandler
   *          the UI language handler
   */
  public void setUiLanguageHandler(UiLanguageHandler uiLanguageHandler) {
    this.uiLanguageHandler = uiLanguageHandler;
  }

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    Assert.notEmpty(this.staticUsers, "The property 'staticUsers' must be assigned");
    Assert.hasText(this.authenticatorName, "The property 'authenticatorName' must be assigned");
    Assert.notNull(this.fallbackLanguages, "The property 'fallbackLanguages' must be assigned");
    Assert.notNull(this.uiLanguageHandler, "The property 'uiLanguageHandler' must be assigned");
  }

}
