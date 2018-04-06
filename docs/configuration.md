
## Configuration parameters for the Swedish eID Reference IdP

This document describes all variables that can be set to control the Shibboleth installation. If you need to change anything else about the Shibboleth installation you need to modify the `dockerStart.sh` or the `standaloneStart.sh` scripts by introducing a mapping between an environment variable and a Shibboleth property setting.


### General settings

| Variable | Description | Default value |
| :--- | :--- | :--- |
| `IDP_APP_NAME` | The IdP application name. | swedish-eid-idp<sup>1</sup> |
| `IDP_ROOT` | The root installation of the IdP. | Docker: `/opt/$APP_NAME`<sup>2</sup><br />Standalone: - |
| `IDP_HOME` | The Shibboleth installation directory within the Docker container. | `$IDP_ROOT/shibboleth` |
| `IDP_TOMCAT_HOME` | The Tomcat instalIDP_TOMCAT_HOMElation directory within the Docker container. | `$IDP_ROOT/tomcat` |
| `IDP_SERVER_HOSTNAME` | The hostname that the IdP exposes externally. | `tools.swedenconnect.se` |
| `IDP_SERVER_SCHEME` | The HTTP scheme. | https |
| `IDP_SERVER_PORT` | The port number for the server. Note: This is the externally exposed port number. | 443 |
| `IDP_CREDENTIALS` | The directory where the IdP credentials are stored. | Docker: `etc/$APP_NAME/credentials`<sup>3</sup><br />Standalone: `$IDP_ROOT/test-credentials` |

> \[1\]: If the application name is changed from its default the `Dockerfile` also needs to be updated.

> \[2\]: If the IdP root directory is changed from its default the `Dockerfile` also needs to be updated.

> \[3\]: Default settings for credentials assumes that a Docker volume has been mounted for the container path `/etc/swedish-eid-idp/credentials`. For test installations this directory should be mapped to the `test-credentials` of the distribution.


### Tomcat settings

| Variable | Description | Default value |
| :--- | :--- | :--- |
| `IDP_TOMCAT_TLS_PORT` | The TLS port used by Tomcat within the Docker container. | 8443 |
| `IDP_TOMCAT_AJP_PORT` | The AJP port used by Tomcat within the Docker container. | 8009 |
| `IDP_TOMCAT_HOSTNAME` | The hostname that the Tomcat serves within the Docker container. | localhost | 
| `IDP_TOMCAT_PROXY_NAME` | The proxy name used if the AJP protocol is used. | `$IDP_SERVER_HOSTNAME` |
| `IDP_TOMCAT_TLS_KEYSTORE` | The keystore holding the TLS server certificate for the Tomcat server. | `$IDP_CREDENTIALS/tomcat/`<br />`localhost-snakeoil.p12` |
| `IDP_TOMCAT_TLS_PASSWORD` | The password unlocking the above keystore. | changeit |
| `IDP_TOMCAT_TLS_ALIAS` | The keystore alias for the key to use for TLS. | localhost |


### IdP settings

| Variable | Description | Default value |
| :--- | :--- | :--- |
| `IDP_ENTITY_ID` | The SAML entityID for the IdP. | `http://${IDP_SERVER_HOSTNAME}/refidp` |
| `IDP_SEALER_STORE_RESOURCE` | The path to the Shibboleth "sealer keystore". | `$IDP_CREDENTIALS/sealer.jks` |
| `IDP_SEALER_PASSWORD` | The password for the Shibboleth "sealer keystore". | `changeme` |
| `IDP_SEALER_VERSION_RESOURCES` | The path to the file holding the version for the sealer. See the [Shibboleth wiki](https://wiki.shibboleth.net/confluence/display/IDP30/SecretKeyManagement). | `$IDP_CREDENTIALS/sealer.kver` |
| `IDP_SIGNING_KEY` | The path to the IdP signature key. | `$IDP_CREDENTIALS/idp-signing.key` |
| `IDP_SIGNING_CERT` | The path to the IdP signature certificate. | `$IDP_CREDENTIALS/idp-signing.crt` |
| `IDP_ENCRYPTION_KEY` | The path to the IdP encryption key. | `$IDP_CREDENTIALS/idp-encryption.key` |
| `IDP_ENCRYPTION_CERT` | The path to the IdP encryption certificate. | `$IDP_CREDENTIALS/idp-encryption.crt` |
| `IDP_METADATA_SIGNING_KEY` | The path to the key used to sign the IdP metadata. | `$IDP_CREDENTIALS/metadata-signing.key` |
| `IDP_METADATA_SIGNING_CERT` | The path to the certificate used to sign the IdP metadata. | `$IDP_CREDENTIALS/metadata-signing.crt` |
| `IDP_PERSISTENT_ID_SALT` | The salt that is used when creating a hash for persistent NameIDs. | `this_needs_to_be_supplied` |


### Metadata settings

| Variable | Description | Default value |
| :--- | :--- | :--- |
| `IDP_FEDERATION_METADATA_URL` | The URL to the federation metadata. | QA metadata for Sweden Connect `https://qa.md.swedenconnect.se/entities` |
| `IDP_FEDERATION_METADATA`<br/>`_VALIDATION_CERT` | The certificate to use when verifying signatures on downloaded metadata for the (Sweden Connect) federation. | `$IDP_CREDENTIALS/trust/`<br />`sc-qa-metadata-validation-cert.crt` |
| `IDP_SECONDARY_FEDERATION`<br />`_METADATA_URL` | Optional URL for additional metadata (e.g., the sandbox federation: https://eid.svelegtest.se/metadata/feed) | - |
| `IDP_SECONDARY_FEDERATION`<br />`_METADATA_VALIDATION_CERT` | Metadata verification certificate needed if a secondary source is used. If the https://eid.svelegtest.se/metadata/feed is used as a secondary source, the certificate $IDP_CREDENTIALS/trust/sveleg-metadata-validation-cert.crt should be given. | - |
| `IDP_METADATA_VALIDITY_MINUTES` | The validity the metadata published by the IdP should have. *Given in minutes*. | 10800 (one week) |
| `IDP_METADATA_CACHEDURATION_MILLIS` | The cache duration to include in the IdP metadata. *Given in milliseconds* | 3600000 (1 hour) | 

### JVM settings

| Variable | Description | Default value |
| :--- | :--- | :--- |
| `JVM_MAX_HEAP` | Max heap size for the JVM. | `1536m` |
| `JVM_START_HEAP` | Initial size for the JVM. | `512m` |

### Logging settings

See [logging.md](logging.md) for how to set up logging.


