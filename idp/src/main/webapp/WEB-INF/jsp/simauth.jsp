<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix ="form" uri ="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>

<html>
  <head>
    <title><spring:message code="sweid.ui.title" /></title>
    
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Swedish eID Reference IdP">

    <meta http-equiv='pragma' content='no-cache'/>
    <meta http-equiv='cache-control' content='no-cache, no-store, must-revalidate'/>
    <meta http-equiv="Expires" content="-1"/>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>    
    
    <c:set var="contextPath" value="${pageContext.request.contextPath}" />
    
    <link rel="stylesheet" type="text/css" href="<c:url value='/bootstrap-3.3.4-dist/css/bootstrap.min.css' />" />
    <link rel="stylesheet" type="text/css" href="<c:url value='/css/authbsstyle.css' />" />
    
  </head>
  <body>
  
    <c:choose>
      <c:when test="${not empty signature and signature.booleanValue() == true}">
        <c:set var="headingMessageCode" value="sweid.ui.signRequest" />
        <c:set var="okButtonMessageCode" value="sweid.ui.signBtn" />
        <c:set var="selectUserOptionMessageCode" value="sweid.ui.sign.select-user-option-text" />        
      </c:when>
      <c:otherwise>
        <c:set var="headingMessageCode" value="sweid.ui.loginRequest" />
        <c:set var="okButtonMessageCode" value="sweid.ui.loginBtn" />
        <c:set var="selectUserOptionMessageCode" value="sweid.ui.auth.select-user-option-text" />
      </c:otherwise>
    </c:choose>
  
    <div style="padding: 20px">
      <div id="mainContainer" class="container">
      
        <table style="height: auto; width: 100%;margin-top: 10px;">
          <tbody>
            <tr>
              <td><img height="70" src="${contextPath}/images/idpLogo.png"/></td>
              <td style="padding-left: 30px;vertical-align: bottom">
                <h2><spring:message code="sweid.ui.title" /></h2>
              </td>
            </tr>
          </tbody>
        </table>
        
        <br/>

        <div class="panel-group">
          <div class="panel panel-primary">
            <div class="panel-heading" id="requesterHeading">
              <spring:message code="${headingMessageCode}" />
            </div>
            <div class="panel-body" id="spinfo" style="min-height: 160px">
              <table class="sptable">
                <tbody>
                  <c:if test="${not empty spInfo.defaultLogoUrl}">
                    <tr>
                      <td>
                        <img src="<c:out value="${spInfo.defaultLogoUrl}" />" />
                      </td>                    
                    </tr>
                  </c:if>
                  <c:choose>
                    <c:when test="${not empty spInfo.displayName}">
                      <tr>
                        <td style="padding-top: 10px;">
                          <c:out value="${spInfo.displayName}" />
                        </td>
                      </tr>
                      <c:if test="${not empty spInfo.description}">
                        <tr>
                          <td style="color: rgb(153, 153, 153); padding-top: 5px;">
                            <c:out value="${spInfo.description}" />
                          </td>
                        </tr>
                      </c:if>
                    </c:when>
                    <c:otherwise>
                      <tr>
                        <td style="padding-top: 10px;">
                          <spring:message code="sweid.ui.sp.default-sp-name" />
                        </td>
                      </tr>
                      <tr>
                        <td style="color: rgb(153, 153, 153); padding-top: 5px;">
                          <spring:message code="sweid.ui.sp.default-sp-desc" />
                        </td>
                      </tr>                                            
                    </c:otherwise>
                  </c:choose>
                                                  
                </tbody>
              </table>
            </div>
          </div>
          
          <c:if test="${not empty signMessage}">
            <div id="sigMessPanel" class="panel panel-info">
              <div class="panel-heading"><spring:message code="sweid.ui.signMessageTitle" /></div>
              <div id="SignMessageBody" class="panel-body" style="height: 160px;overflow: auto">
                ${signMessage.html}
              </div>            
            </div>
          </c:if>  
          
          <div class="panel-default">
            <div class="panel-body">
              <spring:message code='sweid.ui.auth.select-user-option-text' var="selectUserOptionText" />
              
              <form:form modelAttribute="authenticationResult" action="/idp/extauth/simulatedAuth" method="POST" class="form-horizontal" role="form">              
                <div class="form-group">
                  <form:select path="selectedUser" class="form-control">
                    <form:option value="NONE" label="${selectUserOptionText}" />
                    <c:forEach items="${staticUsers}" var="user" varStatus="user_s">
                      <c:choose>
                        <c:when test="${not empty preSelectedUser and preSelectedUser.personalIdentityNumber eq user.personalIdentityNumber}">
                          <option value="${user.personalIdentityNumber}" selected>${user.uiDisplayName}</option>
                        </c:when>
                        <c:otherwise>
                          <option value="${user.personalIdentityNumber}">${user.uiDisplayName}</option>
                        </c:otherwise>
                      </c:choose>                      
                    </c:forEach>
                  </form:select>
                </div>
                <br />
                <div class="form-group">

                  <c:choose>
                    <c:when test="${fn:length(authnContextUris) eq 1}">
                      <form:hidden path="selectedAuthnContextUri" value="${authnContextUris[0]}" />
                    </c:when>
                    <c:otherwise>
                      Tillitsnivå:
                      <br />
                      <form:select path="selectedAuthnContextUri" class="form-control">
                        <c:forEach items="${authnContextUris}" var="uri">
                          <form:option value="${uri}">${uri}</form:option>
                        </c:forEach>
                      </form:select>
                      <br />                      
                    </c:otherwise>                    
                  </c:choose>                  
                </div>
                
                <form:errors path="*" cssClass="form-group alert alert-danger" element="div" />
                
                <form:hidden path="authenticationKey" value="${authenticationKey}" />
                
                <c:choose>
                  <c:when test="${not empty signMessage}">
                    <form:hidden path="signMessageDisplayed" value="true" />
                  </c:when>
                  <c:otherwise>
                    <form:hidden path="signMessageDisplayed" value="false" />
                  </c:otherwise>
                </c:choose>                
                 
                <button type="submit" class="btn btn-danger" name="action" value="cancel"><spring:message code='sweid.ui.cancelBtn' /></button>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <button type="submit" class="btn btn-primary" name="action" value="ok"><spring:message code="${okButtonMessageCode}" /></button>
                
              </form:form>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div id="testuserDiv"></div>
    </body>
    
</html>