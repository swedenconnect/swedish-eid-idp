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
package se.swedenconnect.eid.idp.authn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.CookieGenerator;

import lombok.Setter;
import se.swedenconnect.eid.idp.authn.model.SelectedUserModel;
import se.swedenconnect.eid.idp.authn.model.UiModel;
import se.swedenconnect.eid.idp.config.UiConfigurationProperties.Language;
import se.swedenconnect.eid.idp.users.SimulatedUser;
import se.swedenconnect.eid.idp.users.SimulatedUserDetailsManager;
import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.spring.saml.idp.authentication.Saml2ServiceProviderUiInfo;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.AbstractAuthenticationController;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.RedirectForAuthenticationToken;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatus;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;

/**
 * The controller implementing the user authentication.
 * 
 * @author Martin Lindström
 */
@Controller
public class SimulatedAuthenticationController
    extends AbstractAuthenticationController<SimulatedAuthenticationProvider> {

  public static final String AUTHN_PATH = "/extauth";

  /** The authentication provider that is the "manager" for this authentication. */
  @Setter
  @Autowired
  private SimulatedAuthenticationProvider provider;

  /** The users. */
  @Setter
  @Autowired
  private SimulatedUserDetailsManager userDetailsService;

  /** Possible languages for the UI. */
  @Setter
  @Autowired
  private List<Language> languages;

  /** For saving/getting selected user (and last selected LoA). */
  @Setter
  @Autowired
  @Qualifier("selectedUserCookieGenerator")
  private CookieGenerator selectedUserCookieGenerator;

  /** The cookie generator for saved users. */
  @Setter
  @Autowired
  @Qualifier("savedUsersCookieGenerator")
  private CookieGenerator savedUsersCookieGenerator;

  /** Maximum number of users to save in the above cookie. */
  public static final int MAX_SAVED_USERS = 40;

  /**
   * The entry point for the external authentication process.
   * 
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @return a {@link ModelAndView}
   */
  @GetMapping(AUTHN_PATH)
  public ModelAndView authenticate(final HttpServletRequest request, final HttpServletResponse response) {
    final ModelAndView mav = new ModelAndView("simulated");
    mav.addObject("users", this.getStaticAndSavedUsers(request));

    final RedirectForAuthenticationToken token = this.getInputToken(request);
    final Saml2ServiceProviderUiInfo uiInfo = token.getAuthnInputToken().getUiInfo();

    final UiModel ui = new UiModel();
    this.updateSpUiDisplayItems(ui, uiInfo);
    
    // Check if we have the last user and LoA saved ...
    //
    final Pair<String, String> userAndLoa = this.getSelectedUserAndLoa(request);
    ui.setSelectedUser(userAndLoa.getLeft());
    ui.setSelectedAuthnContextUri(userAndLoa.getRight());

    // Check if we received a PrincipalSelection ...
    //
    final String preSelected = token.getAuthnInputToken().getAuthnRequirements().getPrincipalSelectionAttributes()
        .stream()
        .filter(a -> AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER.equals(a.getId()))
        .filter(a -> !a.getValues().isEmpty())
        .map(a -> a.getValues().get(0))
        .map(String.class::cast)
        .findFirst()
        .orElse(null);
    if (preSelected != null) {
      ui.setSelectedUser(preSelected);
      ui.setFixedSelectedUser(true);
    }

    // Authentication context(s) ...
    //
    ui.setPossibleAuthnContextUris(
        token.getAuthnInputToken().getAuthnRequirements().getAuthnContextRequirements());

    mav.addObject("ui", ui);
    mav.addObject("result", new SelectedUserModel());

    return mav;
  }

  @PostMapping(AUTHN_PATH + "/complete")
  public ModelAndView complete(final HttpServletRequest request, final HttpServletResponse response,
      @RequestParam("action") final String action,
      @ModelAttribute("result") final SelectedUserModel result) {

    if ("cancel".equals(action)) {
      return this.cancel(request);
    }
    else {
      final SimulatedUser user = this.processSelectedUser(request, response, result);
      if (user == null) {
        return this.complete(request, new Saml2ErrorStatusException(Saml2ErrorStatus.UNKNOWN_PRINCIPAL));
      }
      return this.complete(request, new SimulatedAuthenticationToken(user, result.getLoa()));
    }
  }

  /** {@inheritDoc} */
  @Override
  protected SimulatedAuthenticationProvider getProvider() {
    return this.provider;
  }

  /**
   * Updates the MVC model with common attributes such as possible languages.
   *
   * @param model the model
   */
  @ModelAttribute
  public void updateModel(final Model model) {
    final Locale locale = LocaleContextHolder.getLocale();

    model.addAttribute("languages", this.languages.stream()
        .filter(lang -> !lang.getTag().equals(locale.getLanguage()))
        .collect(Collectors.toList()));
  }

  private Pair<String, String> getSelectedUserAndLoa(final HttpServletRequest request) {
    final String selection = Arrays.asList(request.getCookies())
        .stream()
        .filter(c -> c.getName().equals(this.selectedUserCookieGenerator.getCookieName()))
        .map(c -> c.getValue())
        .findFirst()
        .orElse(null);
    if (selection != null) {
      final String[] parts = selection.split("#");
      return ImmutablePair.of(parts[0], parts.length > 1 ? parts[1] : null);
    }
    else {
      return ImmutablePair.nullPair();
    }
  }

  /**
   * Returns a list of the users that have been entered in the "Advanced" view and the static users.
   *
   * @param request the HTTP request
   * @return a list of simulated users
   */
  private List<SimulatedUser> getStaticAndSavedUsers(final HttpServletRequest request) {
    final List<SimulatedUser> users = new ArrayList<>(this.userDetailsService.getUsers());
    users.addAll(this.getSavedUsers(request));
    Collections.sort(users);
    return users;
  }

  /**
   * Returns a list of the users that have been entered in the "Advanced" view.
   *
   * @param request the HTTP request
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
   * Helper method that is invoked when a user has been selected. It will save the selected user in a cookie for future
   * use.
   *
   * @param httpRequest the HTTP request
   * @param httpResponse the HTTP response
   * @param result the result from the view
   * @return the selected user or {@code null}
   */
  private SimulatedUser processSelectedUser(final HttpServletRequest httpRequest,
      final HttpServletResponse httpResponse, final SelectedUserModel result) {

    if (result.getPersonalIdentityNumber() == null) {
      return null;
    }
    // First check among the static users ...
    //
    SimulatedUser user = this.userDetailsService.getUsers().stream()
        .filter(u -> result.getPersonalIdentityNumber().equals(u.getPersonalNumber()))
        .findFirst()
        .orElse(null);

    // Check the custom users ...
    //
    if (user == null) {
      final List<SimulatedUser> savedUsers = this.getSavedUsers(httpRequest);
      user = savedUsers.stream()
          .filter(u -> result.getPersonalIdentityNumber().equals(u.getPersonalNumber()))
          .findFirst()
          .orElse(null);

      if (user == null && result.isCustom()) {
        final List<SimulatedUser> newSavedUsers = new ArrayList<>();
        if (savedUsers.size() >= MAX_SAVED_USERS) {
          // Remove the oldest ...
          newSavedUsers.addAll(savedUsers.subList(1, savedUsers.size()));
        }
        else {
          newSavedUsers.addAll(savedUsers);
        }
        user = new SimulatedUser();
        user.setPersonalNumber(result.getPersonalIdentityNumber());
        user.setGivenName(result.getGivenName());
        user.setSurname(result.getSurname());
        newSavedUsers.add(user);

        // Update the cookie ...
        this.savedUsersCookieGenerator.addCookie(httpResponse, SimulatedUser.encodeList(newSavedUsers));
      }
    }
    if (user == null) {
      return null;
    }

    // Save the selected user in a cookie (for pre-selection the next time).
    //
    this.selectedUserCookieGenerator.addCookie(httpResponse, String.format("%s#%s", user.getPersonalNumber(), result.getLoa()));

    return user;
  }

  private void updateSpUiDisplayItems(final UiModel model, final Saml2ServiceProviderUiInfo uiInfo) {
    final String lang = LocaleContextHolder.getLocale().getLanguage();

    model.setSpDisplayName(Optional.ofNullable(uiInfo.getDisplayName(lang))
        .orElseGet(() -> uiInfo.getDisplayNames().entrySet().stream()
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null)));

    // If there is a logo that is wider than its height, we use that. We also prefer
    // something larger than 40px and less than 100px.
    //
    Saml2ServiceProviderUiInfo.Logotype small = null;
    Saml2ServiceProviderUiInfo.Logotype large = null;
    for (final Saml2ServiceProviderUiInfo.Logotype logo : uiInfo.getLogotypes()) {
      if (logo.getHeight() == null) {
        continue;
      }
      if (logo.getWidth() != null && logo.getWidth() > logo.getHeight()) {
        model.setSpLogoUrl(logo.getUrl());
        break;
      }
      else if (logo.getHeight() > 40 && logo.getHeight() < 100) {
        model.setSpLogoUrl(logo.getUrl());
        break;
      }
      else if (logo.getHeight() < 40) {
        if (small == null || (small != null && logo.getHeight() > small.getHeight())) {
          small = logo;
        }
      }
      else if (logo.getHeight() > 100) {
        if (large == null || (large != null && logo.getHeight() < large.getHeight())) {
          large = logo;
        }
      }
    }
    if (model.getSpLogoUrl() == null) {
      if (large != null) {
        model.setSpLogoUrl(large.getUrl());
      }
      else if (small != null) {
        model.setSpLogoUrl(small.getUrl());
      }
      else if (!uiInfo.getLogotypes().isEmpty()) {
        model.setSpLogoUrl(uiInfo.getLogotypes().get(0).getUrl());
      }
    }
  }

}
