<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>

<html>
  <head>
  <title><spring:message code="sweid.ui.title" /></title>

  <c:set var="contextPath" value="${pageContext.request.contextPath}" />

  <script type="text/javascript" src="${contextPath}/js/jquery-1.11.3.min.js"></script>
  <script type="text/javascript" src="${contextPath}/js/jquery_cookie.js"></script>
  <script type="text/javascript" src="${contextPath}/js/testconf.js"></script>

  <link rel="stylesheet" type="text/css" href="${contextPath}/css/authstyle.css" />

  </head>
  <body>

    <img class="logo" src="${contextPath}/images/idpLogo.png" />
    <div class="mainIdpFrame">
      <h1><spring:message code="sweid.ui.title" /></h1>
      <h2><spring:message code="sweid.ui.autoAuthSubtitle" /></h2>
    
      <p><spring:message code="sweid.ui.autoAuthSelectUser" /><p>
    
      <table>
        <tr>
          <td>
            <div class="mainAuthFrame">
              <form action="/idp/extauth/autoauth/action" method="POST">
                <div id="testuserDiv">
                  <table class="demoUserTableClass">
                    <c:forEach var="user" items="${staticUsers}">
                      <tr class="demouserrow">                      
                        <td>
                          <input type="radio" name="selectedUser" value="${user.personalIdentityNumber}" <c:out value="${selectedUserId == user.personalIdentityNumber ? 'checked' : ''}" />  />
                        </td>
                        <td><c:out value="${user.displayName}" /></td>
                        <td><c:out value="${user.personalIdentityNumber}" /></td>
                      </tr>
                    </c:forEach>
                  </table>
                </div>
                <br />

                <button type="submit" id="saveButton" name="action" value="save"><spring:message code='sweid.ui.autoAuthSelectBtn' /></button>
                <button type="submit" id="eraseButton" name="action" value="clear"><spring:message code='sweid.ui.autoAuthCancelBtn' /></button>
              </form>
            </div>
          </td>
          <td valign="top">
            <div id="currentSetting">
            
              <c:choose>
                <c:when test="${not empty selectedUserId}">
                  <spring:message code="sweid.ui.autoauth-active-yes" var="activeFlagText" />
                  <c:set var="selectedUserText" value="${selectedUserId}" />
                </c:when>
                <c:otherwise>
                  <spring:message code="sweid.ui.autoauth-active-no" var="activeFlagText" />
                  <spring:message code="sweid.ui.not-selected" var="selectedUserText" />
                </c:otherwise>
              </c:choose>
            
              <table>
                <tr>
                  <td><i><spring:message code='sweid.ui.autoauth-active-title' />:</i></td>
                  <td><b><span id="autoAuthActiveFlag">${activeFlagText}</span></b></td>
                </tr>
                <tr>
                  <td><i><spring:message code='sweid.ui.selected-user' />:</i></td>
                  <td><b><span id="selectedUserId">${selectedUserText}</span></b></td>
                </tr>
              </table>
            
            </div>
          </td>
          
        </tr>
               
      </table>
      
    </div>
    
    <div id="notSelectedText" style="display: none;"><spring:message code='sweid.ui.not-selected' /></div>
    <div id="activeYesText" style="display: none;"><spring:message code='sweid.ui.autoauth-active-yes' /></div>
    <div id="activeNoText" style="display: none;"><spring:message code='sweid.ui.autoauth-active-no' /></div>
    
  </body>
</html>