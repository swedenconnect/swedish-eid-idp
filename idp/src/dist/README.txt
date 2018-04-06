
Reference IdP for the Swedish eID Framework
===========================================

This distribution comprises of all components needed for running an instance of the
Swedish eID Reference Identity Provider.

Contents
--------

- tomcat
    A Tomcat instance with the a built Shibboleth/Swedish eID war-file placed under webapps.
    
- shibboleth
    A customized version of Shibboleth implementing the reference IdP for the Swedish eID
    Framework.
    
- test-credentials
    Keys and certificates for running the IdP ... In test mode that is. Of course you should
    have your own keys and certificates when running as a production IdP.
    
- scripts
    Start-up scripts for the IdP. They are:
     - standaloneStart.sh: A start script for running the IdP in stand-alone mode using the
       embedded Tomcat. 
     - dockerStart.sh: A start script for starting the IdP in a Docker container. This script
       is added to the Docker image by the Dockerfile.
     - runStandalone.sh: An example script of how the IdP may be configured to run in stand-alone
       mode from a distribution.
     - runDevStandalone.sh: An example script of how the IdP may be configured to run in stand-alone
       mode directly after a build (suitable for development).
     - runDocker.sh: An example script of how to start the Docker container running the IdP.
         
- Dockerfile
    A Dockerfile for building a docker image for the reference IdP.
    
Configuration
-------------

See https://github.com/elegnamnden/swedish-eid-idp/blob/master/README.md

Running the IdP stand-alone
---------------------------

- Unzip the distribution (target/swedish-eid-idp-<version>.zip)
- Create a start-script (see the example script runStandalone.sh)
- Run ...

Running the IdP in a Docker image
---------------------------------

- Unzip the distribution (target/swedish-eid-idp-<version>.zip)
- Build a docker image:
  > docker build . -t my-own-idp

- Create a start-script (see the example script runDocker.sh)
- Run ...
  
