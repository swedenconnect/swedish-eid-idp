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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.profile.context.ProfileRequestContext;
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

import com.google.common.base.Function;
import com.google.common.base.Functions;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import se.elegnamnden.eid.idp.authn.model.SimulatedAuthenticationResult;
import se.elegnamnden.eid.idp.authn.model.SimulatedUser;
import se.litsec.shibboleth.idp.authn.context.AuthnContextClassContext;
import se.litsec.shibboleth.idp.authn.context.strategy.AuthenticationContextLookup;
import se.litsec.shibboleth.idp.authn.context.strategy.AuthnContextClassContextLookup;
import se.litsec.shibboleth.idp.authn.controller.AbstractExternalAuthenticationController;
import se.litsec.swedisheid.opensaml.saml2.authentication.LevelofAssuranceAuthenticationContextURI.LoaEnum;

/**
 * A Spring MVC-controller for the IdP's authentication process. This is a simulated authentication where the user
 * simply selects which individual to authenticate as.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 * @author Stefan Santesson (stefan@aaa-sec.com)
 */
@Controller
public class SimulatedAuthenticationController extends AbstractExternalAuthenticationController implements InitializingBean {

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

  /**
   * Strategy that gives us the AuthenticationContext. Needed since the authnContextService property of
   * AbstractExternalAuthenticationController is private (will be fixed).
   */
  @SuppressWarnings("rawtypes") protected static Function<ProfileRequestContext, AuthenticationContext> authenticationContextLookupStrategy = new AuthenticationContextLookup();

  /**
   * Strategy used to locate the AuthnContextClassContext. Needed since the authnContextService property of
   * AbstractExternalAuthenticationController is private (will be fixed).
   */
  @SuppressWarnings("rawtypes") protected static Function<ProfileRequestContext, AuthnContextClassContext> authnContextClassLookupStrategy = Functions
    .compose(new AuthnContextClassContextLookup(), authenticationContextLookupStrategy);

  /** {@inheritDoc} */
  @Override
  public String getAuthenticatorName() {
    return this.authenticatorName;
  }

  /** {@inheritDoc} */
  @Override
  protected ModelAndView doExternalAuthentication(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String key,
      ProfileRequestContext<?, ?> profileRequestContext) throws ExternalAuthenticationException, IOException {

    AutoAuthnCookie autoAuthnCookie = Arrays.asList(httpRequest.getCookies())
      .stream()
      .filter(c -> c.getName().equals(AutoAuthnCookie.AUTO_AUTHN_COOKIE_NAME))
      .map(Cookie::getValue)
      .map(v -> AutoAuthnCookie.parse(v))
      .findFirst()
      .orElse(null);
    if (autoAuthnCookie != null) {
      return this.processAuthentication(httpRequest, httpResponse, key, null, new SimulatedAuthenticationResult(autoAuthnCookie
        .getPersonalIdentityNumber()));
    }

    ModelAndView modelAndView = new ModelAndView("simauth");
    modelAndView.addObject("authenticationKey", key);
    modelAndView.addObject("authenticationResult", new SimulatedAuthenticationResult());

    Locale locale = LocaleContextHolder.getLocale();
    modelAndView.addObject("spInfo",
      SpInfoHandler.buildSpInfo(this.getPeerMetadata(profileRequestContext), locale.getLanguage(), this.fallbackLanguages));

    return modelAndView;
  }

  /**
   * Processes the result sent in from the view.
   * 
   * @param httpRequest
   *          the HTTP request
   * @param httpResponse
   *          the HTTP response
   * @param key
   *          the Shibboleth external authentication key
   * @param result
   *          the "authentication result"
   * @param bindingResult
   *          the result from the binding operation
   * @return a {@code ModelAndView} if control is to return to the view or {@code null} to terminate the operation
   * @throws ExternalAuthenticationException
   *           for Shibboleth session errors
   * @throws IOException
   *           for IO errors
   */
  @RequestMapping(value = "/simulatedAuth", method = RequestMethod.POST)
  public ModelAndView processAuthentication(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
      @RequestParam("authenticationKey") String key,
      @RequestParam("action") String action,
      @ModelAttribute("authenticationResult") SimulatedAuthenticationResult result) throws ExternalAuthenticationException, IOException {

    if ("cancel".equals(action)) {
      this.cancel(httpRequest, httpResponse);
      return null;
    }

    if ("NONE".equals(result.getSelectedUser())) {
      logger.debug("Binding errors, returning control to the view");
      ModelAndView modelAndView = new ModelAndView("simauth");
      modelAndView.addObject("authenticationKey", key);
      modelAndView.addObject("authenticationResult", result);
      modelAndView.addObject("spInfo",
        SpInfoHandler.buildSpInfo(this.getPeerMetadata(httpRequest), LocaleContextHolder.getLocale().getLanguage(),
          this.fallbackLanguages));

      return modelAndView;
    }

    if (result.getSelectedUser() != null) {
      SimulatedUser user = this.staticUsers.stream()
        .filter(u -> result.getSelectedUser().equals(u.getPersonalIdentityNumber()))
        .findFirst()
        .orElse(null);

      if (user != null) {

        logger.debug("Authenticated user: {}", user);

        // Save the selected user in a cookie (for pre-selection the next time).
        //
        this.saveSelectedUser(user.getPersonalIdentityNumber(), httpResponse);

        // Get hold of the requested AuthnContextClass context. Here we calculate which AuthnContextClassRef to return.
        //
        AuthnContextClassContext authnContextClassContext = authnContextClassLookupStrategy.apply(this.getProfileRequestContext(
          httpRequest));
        String authnContextClassRef = null;
        if (authnContextClassContext != null && !authnContextClassContext.getAuthnContextClassRefs().isEmpty()) {
          authnContextClassRef = authnContextClassContext.getAuthnContextClassRefs().get(0);
        }
        if (authnContextClassRef == null) {
          // TODO: Make configurable
          authnContextClassRef = LoaEnum.LOA_3.getUri();
        }

        Subject subject = this.getSubjectBuilder(user.getPersonalIdentityNumber())
          .shibbolethAttribute("personalIdentityNumber", user.getPersonalIdentityNumber())
          .shibbolethAttribute("givenName", user.getGivenName())
          .shibbolethAttribute("sn", user.getSurname())
          .shibbolethAttribute("displayName", user.getDisplayName())
          .authnContextClassRef(authnContextClassRef)
          .build();

        this.success(httpRequest, httpResponse, subject, null, null);
        return null;
      }
    }

    logger.error("No user selected");
    this.error(httpRequest, httpResponse, AuthnEventIds.INVALID_AUTHN_CTX);
    return null;
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
  @ModelAttribute("preSelectedUser")
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
   * <p>
   * Since this method is annotated with {@code ModelAttribute("staticUsers)} it means that the JSP-views automatically
   * will get this list of {@link SimulatedUser} objects assigned to the variable "staticUsers".
   * </p>
   * 
   * @return the static list of simulated users.
   */
  @ModelAttribute("staticUsers")
  public List<SimulatedUser> getStaticUsers() {
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

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    Assert.notEmpty(this.staticUsers, "The property 'staticUsers' must be assigned");
    Assert.hasText(this.authenticatorName, "The property 'authenticatorName' must be assigned");
    Assert.notNull(this.fallbackLanguages, "The property 'fallbackLanguages' must be assigned");
  }

}
