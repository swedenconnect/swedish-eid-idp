
## Logging Configuration

This document describes how logging is configured for the Swedish eID Reference IdP. 

### Startup variables controlling logging

The table below lists all logging related variables that may be passed to Docker when the container is started.

| Variable | Description | Default value |
| :--- | :--- | :--- |
| `IDP_LOG_SETTINGS_FILE` | Path to a Logback include file that may be used to override the default log levels of the IdP. See [Setting log levels]("setting-log-levels) below. | - |
| `IDP_LOG_CONSOLE` | Set to `true` to make Shibboleth and Tomcat log only to the console (stdout). This does not apply for audit logging. | false |
| `IDP_SYSLOG_HOST` | Specifies the syslog host where audit (and F-TICKS) entries should be sent. If this variable is not set, audit entries will be written to file instead. | - |
| `IDP_SYSLOG_PORT` | Specifies the syslog port to use. | 514 |
| `IDP_AUDIT_SYSLOG_FACILITY` | The syslog facility to use when logging Audit entries to syslog. | AUTH |



<a name="setting-log-levels"></a>
### Setting log levels

Logging in Shibboleth is configured using a Logback configuration file. The Shibboleth default-settings are left untouched and they are listed here in the [Shibboleth Wiki](https://wiki.shibboleth.net/confluence/display/IDP30/LoggingConfiguration#LoggingConfiguration-VariablesandProperties).

We have also added another level for code from Litsec and E-legitimationsnämnden. They are:

| Variable | Default | Function |
| :--- | :--- | :--- |
| idp.loglevel.eln | INFO | General code from se.elegnamnden packages. |


In a production system, the Shibboleth default may be a bit too chatty, and therefore it is possible to override the default settings by supplying a Logback include file. This is done by setting the `IDP_LOG_SETTINGS_FILE` to the path where this include file resides.

Example on a production Logback configuration:

```
<included>    <variable name="idp.loglevel.idp" value="INFO" />    <variable name="idp.loglevel.messages" value="ERROR" />    <variable name="idp.loglevel.encryption" value="ERROR" />    <variable name="idp.loglevel.opensaml" value="ERROR" />    <variable name="idp.loglevel.props" value="ERROR" />        <variable name="idp.loglevel.eln" value="INFO" />    <variable name="idp.loglevel.spring" value="ERROR" />    <variable name="idp.loglevel.container" value="ERROR" />    <variable name="idp.loglevel.xmlsec" value="ERROR" />    <!--       It is also possible to add loggers and even appenders in this override file.            For example:      <logger name="net.shibboleth.idp.saml.attribute.mapping" level="INFO" />     --></included>
```

The Shibboleth logging system is reloaded periodically, so it is possible to change any log settings for a running system. It is also possible to force a reload of the logging system by issuing a GET request to:

```
GET https://<idp-host>/idp/profile/admin/reload-service?id=shibboleth.LoggingService
```
Note: Access to admin endpoints are restricted and can only be accessed from the host where the IdP runs.
