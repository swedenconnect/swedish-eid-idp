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
    
    <link rel="stylesheet" type="text/css" href="<c:url value='/bootstrap/css/bootstrap.min.css' />" >
    <link rel="stylesheet" type="text/css" href="<c:url value='/css/open-sans-fonts.css' />" >
    <link rel="stylesheet" type="text/css" href="<c:url value='/css/refmain.css' />" >    

  </head>
  <body>
  
    <!-- Logotypes -->
    <div class="container-fluid header">
      <div class="container">        
        <div class="row">
          <div class="col-sm-12 top-logo">
            <img class="top-logo-dim float-left" src="<c:url value='/images/idp-logo.svg' />" />
          </div>
        </div>
      </div>
    </div> <!-- /.header -->
    
    <div class="container main">
    
      <div class="row"><div class="col-sm-12">            
        <span class="lang float-right">&nbsp;</span>
      </div></div>            
    
      <div class="row" id="autoAuthnDiv">
    
        <form action="/idp/extauth/autoauth/action" method="POST">
        
          <div class="col-sm-12 content-container">
          
            <div class="row" id="infoText">
              <div class="col-sm-12 content-heading">
                <h2><spring:message code="sweid.ui.title" /></h2>
                <h3><spring:message code="sweid.ui.auto-authn.subtitle" /></h3>
              </div>              
              <div class="col-sm-12">                
                <p class="info">
                  <spring:message code="sweid.ui.auto-authn.select-user" />              
                </p>
              </div>
            </div> <!-- /#infoText -->
                        
            <hr class="full-width" />
            
            <div class="row section" id="selectAutoUserDiv">
              <div class="col-sm-12">
                <select class="form-control" id="selectSimulatedUser" name="selectedUser">
                  <option value="NONE"><spring:message code="sweid.ui.auto-authn.select-label" /></option>
                  <c:forEach items="${staticUsers}" var="user" varStatus="user_s">
                    <option value="${user.personalIdentityNumber}" <c:out value="${selectedUserId == user.personalIdentityNumber ? 'selected' : ''}" />>
                      ${user.uiDisplayName}
                    </option>
                  </c:forEach>
                </select>
              </div>            
            </div> <!-- /#selectAutoUserDiv -->
            
            <div class="row section" id="submitDiv">
              <div class="col-12">
                <div class="box">
                  <button type="submit" id="saveButton" class="btn btn-primary" name="action" value="save">
                    <spring:message code='sweid.ui.auto-authn.button.select' />
                  </button>
                  <button type="submit" id="eraseButton" class="btn btn-primary" name="action" value="clear">
                    <spring:message code='sweid.ui.auto-authn.button.clear' />
                  </button>                  
                </div>
              </div>
            </div> <!-- /#submitDiv -->            
          
          </div> <!-- /.content-container -->
      
        </form>
        
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
      
      </div> <!-- /.autoAuthnDiv -->
    
    </div> <!-- /.container main -->
    
    <script src="<c:url value='/js/jquery-3.3.1.slim.min.js' />" type="text/javascript"></script>
    <script src="<c:url value='/js/popper-1.14.0.min.js' />" type="text/javascript"></script>
    <script src="<c:url value='/bootstrap/js/bootstrap.min.js' />" type="text/javascript"></script>
    <script src="<c:url value='/js/testconf.js' />" type="text/javascript"></script>    
  </body>
</html>