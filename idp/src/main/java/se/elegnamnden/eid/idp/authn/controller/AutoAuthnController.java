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

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import se.elegnamnden.eid.idp.authn.model.SimulatedUser;

/**
 * Spring MVC controller for the auto-authentication test feature.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 * @author Stefan Santesson (stefan@aaa-sec.com)
 */
@Controller
public class AutoAuthnController implements InitializingBean {

  /** Logging instance. */
  private final Logger logger = LoggerFactory.getLogger(AutoAuthnController.class);

  /** The path that this controller is mapped against relative to the servlet context path. */
  private String controllerPath = "";

  /** A list of simulated users that we offer as a choice when authenticating. */
  private List<SimulatedUser> staticUsers;

  /**
   * Main request mapping.
   * 
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   * @param authnCookieValue
   * @return
   */
  @RequestMapping(value = "/autoauth", method = RequestMethod.GET)
  public ModelAndView viewAutoAuthnPage(HttpServletRequest request, HttpServletResponse response,
      @CookieValue(value = AutoAuthnCookie.AUTO_AUTHN_COOKIE_NAME, required = false) String authnCookieValue) {

    ModelAndView modelAndView = new ModelAndView("testconf");

    AutoAuthnCookie cookie = AutoAuthnCookie.parse(authnCookieValue);
    if (cookie != null) {
      // Make sure the cookie contains a valid user.
      if (this.staticUsers.stream()
        .filter(u -> cookie.getPersonalIdentityNumber().equals(u.getPersonalIdentityNumber()))
        .findFirst()
        .isPresent()) {
        modelAndView.addObject("selectedUserId", cookie.getPersonalIdentityNumber());
      }
      else {
        logger.error("Cookie '{}' contained value '{}' - this is not a valid user", AutoAuthnCookie.AUTO_AUTHN_COOKIE_NAME, cookie
          .getPersonalIdentityNumber());
      }
    }

    return modelAndView;
  }

  /**
   * Method that will only be invoked if the user makes a POST. This only happens if he or she does not have JavaScript
   * enabled.
   * 
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   * @param action
   *          the post action (save or clear)
   * @param selectedUser
   *          the selected user id (set if action is "save")
   * @return a model and view pointing back to the start page
   */
  @RequestMapping(value = "/autoauth/action", method = RequestMethod.POST)
  public ModelAndView processAndSave(HttpServletRequest request, HttpServletResponse response,
      @RequestParam("action") String action, @RequestParam(value = "selectedUser", required = false) String selectedUser) {

    if ("save".equals(action) && selectedUser != null) {
      this.save(request, response, selectedUser);
    }
    else {
      this.reset(request, response);
    }

    return new ModelAndView("redirect:" + this.controllerPath + "/autoauth");
  }

  /**
   * Saves the selected user in a cookie.
   * 
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   * @param selectedUser
   *          the user id for the user to save
   */
  @RequestMapping(value = "/autoauth/save", method = RequestMethod.POST)
  @ResponseStatus(value = HttpStatus.OK)
  public void save(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "selectedUser") String selectedUser) {
    if ("NONE".equals(selectedUser)) {
      return;
    }
    AutoAuthnCookie autoAuthnCookie = new AutoAuthnCookie();
    autoAuthnCookie.setPersonalIdentityNumber(selectedUser);
    Cookie cookie = new Cookie(AutoAuthnCookie.AUTO_AUTHN_COOKIE_NAME, autoAuthnCookie.getCookieValue());
    cookie.setPath("/idp");
    response.addCookie(cookie);
  }

  /**
   * Removes the cookie that stores the selected user.
   * 
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   */
  @RequestMapping(value = "/autoauth/reset", method = RequestMethod.POST)
  @ResponseStatus(value = HttpStatus.OK)
  public void reset(HttpServletRequest request, HttpServletResponse response) {
    Cookie cookie = new Cookie(AutoAuthnCookie.AUTO_AUTHN_COOKIE_NAME, null);
    cookie.setPath("/idp");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
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
   * Assigns the path that this controller is mapped against relative the servlet context path. The default is "/".
   * 
   * @param controllerPath
   *          the path to assign
   */
  public void setControllerPath(String controllerPath) {
    this.controllerPath = controllerPath;
  }

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notEmpty(this.staticUsers, "The property 'staticUsers' must be assigned");
  }

}
