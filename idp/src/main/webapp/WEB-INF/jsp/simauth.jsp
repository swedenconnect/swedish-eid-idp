<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>

<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="Swedish eID Reference IdP">

    <meta http-equiv='pragma' content='no-cache'/>
    <meta http-equiv='cache-control' content='no-cache, no-store, must-revalidate'/>
    <meta http-equiv="Expires" content="-1"/>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>    
    
    <link rel="stylesheet" type="text/css" href="<c:url value='/css/bootstrap-4.1.0.min.css' />" >
    <link rel="stylesheet" type="text/css" href="<c:url value='/css/open-sans-fonts.css' />" >
    <!-- <link href="https://fonts.googleapis.com/css?family=Open+Sans:300,400,400i,700" rel="stylesheet"> -->
    <link rel="stylesheet" type="text/css" href="<c:url value='/css/refmain.css' />" >    
        
    <title><spring:message code="sweid.ui.title" /></title>
    
  </head>
  <body>
  
    <c:choose>
      <c:when test="${simulatedAuthentication.signature == true}">
        <c:choose>
          <c:when test="${not empty simulatedAuthentication.signMessage}">
            <c:choose>
              <c:when test="${simulatedAuthentication.fixedSelectedUser == true}">         
                <c:set var="infoTextCode" value="sweid.ui.sp-info-text.sign-noselect" />
              </c:when>
              <c:otherwise>
                <c:set var="infoTextCode" value="sweid.ui.sp-info-text.sign" />
              </c:otherwise>
            </c:choose>
          </c:when>
          <c:otherwise>
            <c:choose>
              <c:when test="${simulatedAuthentication.fixedSelectedUser == true}">
                <c:set var="infoTextCode" value="sweid.ui.sp-info-text.sign-notext-noselect" />
              </c:when>
              <c:otherwise>
                <c:set var="infoTextCode" value="sweid.ui.sp-info-text.sign-notext" />
              </c:otherwise>
            </c:choose>
          </c:otherwise>
        </c:choose>
        <c:set var="okButtonMessageCode" value="sweid.ui.button.sign" />        
        <c:set var="selectUserOptionMessageCode" value="sweid.ui.sign.select-user-option-text" />
        <c:set var="loaInfoTextCode" value="sweid.ui.sign.authn-context-class.info" />
        <c:set var="loaSelectTextCode" value="sweid.ui.sign.authn-context-class.label" />
      </c:when>
      <c:otherwise>
        <c:choose>
          <c:when test="${simulatedAuthentication.fixedSelectedUser == true}">
            <c:set var="infoTextCode" value="sweid.ui.sp-info-text.auth-noselect" />
          </c:when>
          <c:otherwise>
            <c:set var="infoTextCode" value="sweid.ui.sp-info-text.auth" />        
          </c:otherwise>
        </c:choose>
        <c:set var="okButtonMessageCode" value="sweid.ui.button.login" />
        <c:set var="selectUserOptionMessageCode" value="sweid.ui.auth.select-user-option-text" />
        <c:set var="loaInfoTextCode" value="sweid.ui.auth.authn-context-class.info" />
        <c:set var="loaSelectTextCode" value="sweid.ui.auth.authn-context-class.label" />
      </c:otherwise>
    </c:choose>    
    
    <!-- Logotypes -->
    <div class="container-fluid header">
      <div class="container">        
        <div class="row">
          <c:choose>
            <c:when test="${not empty simulatedAuthentication.spInfo.defaultLogoUrl}">
              <div class="col-6 top-logo">
                <img class="top-logo-dim float-left" src="<c:out value="${simulatedAuthentication.spInfo.defaultLogoUrl}" />" alt="Logo" />
              </div>
              <div class="col-6 top-logo">
                <img class="top-logo-dim float-right" src="<c:url value='/images/ref-idp-logo.svg' />" />
              </div>
            </c:when>
            <c:otherwise>
              <div class="col-sm-12 top-logo">
                <img class="top-logo-dim float-left" src="<c:url value='/images/ref-idp-logo.svg' />" />
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div> <!-- /.header -->
    
    <div class="container main">

      <!-- Language -->
      <form action="/idp/extauth/startAuth" method="POST">              
        <div class="row" id="languageDiv">
          <div class="col-sm-12">
            <c:choose>
              <c:when test="${not empty uiLanguages}">
                <c:forEach items="${uiLanguages}" var="uiLang">
                  <button class="lang float-right btn btn-link" type="submit" 
                          value="${uiLang.languageTag}" name="language" id="language_${uiLang.languageTag}">${uiLang.text}</button>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <span class="lang float-right">&nbsp;</span>
              </c:otherwise>
            </c:choose>
          </div>
        </div> <!-- /#languageDiv -->
      </form>
      
      <form:form modelAttribute="simulatedAuthentication" action="/idp/extauth/simulatedAuth" method="POST">
      
        <div class="row" id="authnDiv">

          <div class="col-sm-12 content-container">
        
            <div class="row" id="spInfoText">
              <div class="col-sm-12 content-heading">
                <h2><spring:message code="sweid.ui.title" /></h2>
              </div>
              <div class="col-sm-12">
                <p class="info">
                  <spring:message code="sweid.ui.default-sp-name" var="defaultName" />
                  <c:set var="displayName" value="${not empty simulatedAuthentication.spInfo.displayName ? simulatedAuthentication.spInfo.displayName : defaultName}" />
                  <spring:message code="${infoTextCode}" arguments="${displayName}" />              
                </p>
                <c:if test="${empty simulatedAuthentication.possibleAuthnContextUris}">
                  <p class="info">
                    <spring:message code="${loaInfoTextCode}" arguments="${simulatedAuthentication.selectedAuthnContextUri}" />
                  </p>
                </c:if>
              </div>
            </div> <!-- /#spInfoText -->
          
            <hr class="full-width" />
          
            <c:if test="${not empty simulatedAuthentication.signMessage}">
              <!-- Sign message -->
              <div class="full-width sign-message">
                <div class="row no-gutters">
                  <div class="col">
                    <c:out value="${simulatedAuthentication.signMessage.html}" escapeXml="false" />
                  </div>
                </div>
              </div> <!-- /.sign-message -->
            </c:if>
                              
            <form:hidden path="signMessageDisplayed" value="${not empty simulatedAuthentication.signMessage ? 'true' : 'false'}" />            
            <form:hidden path="authenticationKey" value="${simulatedAuthentication.authenticationKey}" />
          
            <div class="row section" id="selectSimulatedUserDiv">
              <div class="col-sm-12">
                <form:select path="selectedUser" class="form-control" id="selectSimulatedUser" disabled="${simulatedAuthentication.fixedSelectedUser}">
                  <spring:message code="${selectUserOptionMessageCode}" var="selectUserText" />
                  <form:option value="NONE" label="${selectUserText}" /> 
                  <c:forEach items="${staticUsers}" var="user" varStatus="user_s">
                    <form:option value="${user.personalIdentityNumber}">${user.uiDisplayName}</form:option>
                  </c:forEach>
                </form:select>
                
                <c:if test="${simulatedAuthentication.fixedSelectedUser == false}">
                <div class="noscripthide">
                  <button id="advancedButton" class="btn btn-link float-right" type="button"><spring:message code="sweid.ui.button.advanced" /> &gt;&gt;</button>
                </div>
                </c:if>
              </div>            
            </div> <!-- /#selectSimulatedUserDiv -->
            <c:if test="${simulatedAuthentication.fixedSelectedUser == true}">
              <form:hidden path="selectedUser" value="${simulatedAuthentication.selectedUser}" />
            </c:if>
            
            <div id="advancedSettings" class="row section noscripthide">
              <div class="col-sm-12">
                <div class="advanced">
                  <div class="box">
                    <div class="form-group row">
                      <label for="personalIdNumber" class="col-sm-3 col-form-label"><spring:message code="sweid.ui.advanced.personal-id.label" /></label>
                      <div class="col-sm-9">
                        <spring:message code="sweid.ui.advanced.personal-id.placeholder" var="personalIdNumberPlaceholder" />
                        <form:input path="selectedUserFull.personalIdentityNumber" id="personalIdNumber" class="form-control" 
                          placeholder="${personalIdNumberPlaceholder}" />
                          <!-- <input type="text" class="form-control" placeholder="12 digits" id="personalIdNumber"> -->
                        <div id="badPersonalIdNumber" class="invalid-feedback"><spring:message code="sweid.ui.advanced.personal-id.bad" /></div>
                      </div>
                    </div>
                    <div class="form-group row">
                      <label for="givenName" class="col-sm-3 col-form-label"><spring:message code="sweid.ui.advanced.given-name.label" /></label>
                      <div class="col-sm-9">
                        <spring:message code="sweid.ui.advanced.given-name.placeholder" var="givenNamePlaceholder" />
                        <form:input path="selectedUserFull.givenName" id="givenName" class="form-control" placeholder="${givenNamePlaceholder}" />
                        <!-- <input type="text" class="form-control" placeholder="Enter given name ..." id="givenName"> -->
                      </div>
                    </div>
                    <div class="form-group row">
                      <label for="surname" class="col-sm-3 col-form-label"><spring:message code="sweid.ui.advanced.surname.label" /></label>
                      <div class="col-sm-9">
                        <spring:message code="sweid.ui.advanced.surname.placeholder" var="surnamePlaceholder" />
                        <form:input path="selectedUserFull.surname" id="surname" class="form-control" placeholder="${surnamePlaceholder}" />
                        <!-- <input type="text" class="form-control" placeholder="Enter surname ..." id="surname"> -->
                      </div>
                    </div>
                    <fieldset class="form-group">
                      <div class="row">
                        <div class="col-sm-12">
                          <button id="cancelAdvancedButton" class="btn btn-link float-right" type="button">
                            <spring:message code="sweid.ui.button.cancel" />
                          </button>                        
                        </div>
                      </div>
                    </fieldset>
                  </div>
                </div>
              </div>
            </div>            
            
            <c:if test="${not empty simulatedAuthentication.possibleAuthnContextUris}">
              <div class="row section" id="selectLoaDiv">
                <div class="col-sm-12">
                  <div class="form-group">
                    <label for="selectLoa"><spring:message code="${loaSelectTextCode}" />:</label>
                    <form:select path="selectedAuthnContextUri" class="form-control" id="selectLoa">
                      <c:forEach items="${simulatedAuthentication.possibleAuthnContextUris}" var="loa" varStatus="loa_s">
                        <form:option value="${loa}">${loa}</form:option>
                      </c:forEach>
                    </form:select>
                  </div>
                </div>            
              </div> <!-- /#selectLoaDiv -->
            </c:if>
            
            <div class="row section" id="submitDiv">
              <div class="col-12">
                <div class="box">
                  <button id="submitButton" type="submit" class="btn btn-primary" name="action" value="ok">
                    <spring:message code="${okButtonMessageCode}" />
                  </button>
                </div>
              </div>
            </div> <!-- /#submitDiv -->
            
            <form:errors path="*" cssClass="form-group alert alert-danger" element="div" />
            
            <div class="drop-down-container noscripthide">

              <div class="col-sm-12 drop-down">
                <p><spring:message code='sweid.ui.help.1.title' /></p>
                <div class="drop-down-info"><spring:message code='sweid.ui.help.1.text' /></div>
              </div>
              <div class="col-sm-12 drop-down">
                <p><spring:message code='sweid.ui.help.2.title' /></p>
                <div class="drop-down-info"><spring:message code='sweid.ui.help.2.text' /></div>
              </div>
              <div class="col-sm-12 drop-down">
                <p><spring:message code='sweid.ui.help.3.title' /></p>
                <div class="drop-down-info"><spring:message code='sweid.ui.help.3.text' /></div>
              </div>
              
            </div> <!-- /.drop-down-container -->
        
          </div> <!-- ./col-sm-12 content-container -->
          
          <div class="col-sm-12 return">
            <button class="btn btn-link" type="submit" name="action" value="cancel">
              <spring:message code="sweid.ui.button.cancel-return" />
            </button>
          </div>          
        
          <div class="col-sm-12 copyright">
            <div class="row">
              <div class="col-6">
                <img class="float-left" src="<c:url value='/images/idp-logo.svg' />" height="40" /> 
              </div>
              <div class="col-6">
                <p class="float-right"><spring:message code="sweid.ui.copyright" /></p>
              </div>
            </div>
          </div>        
      
        </div> <!-- /#authnDiv -->
    
      </form:form>
    
    </div> <!-- /.container main -->
    
    <script>
      var users = [];
      
      <c:forEach items="${staticUsers}" var="user" varStatus="user_s">
      users.push({
        "pnr" : "${user.personalIdentityNumber}",
        "givenName" : "${user.givenName}",
        "surname" : "${user.surname}"
      });    
      </c:forEach>
          
    </script>
    
    <script src="<c:url value='/js/jquery-3.3.1.slim.min.js' />" type="text/javascript"></script>
    <script src="<c:url value='/js/popper-1.14.0.min.js' />" type="text/javascript"></script>
    <script src="<c:url value='/js/bootstrap-4.1.0.min.js' />" type="text/javascript"></script>
    <script src="<c:url value='/js/valfor.js' />" type="text/javascript"></script>
    <script src="<c:url value='/js/refmain.js' />" type="text/javascript"></script>

  </body>
    
</html>