![Logo](https://github.com/swedenconnect/technical-framework/blob/master/img/sweden-connect.png)

# swedish-eid-idp

Reference Identity Provider for the Swedish eID Framework

---

The swedish-eid-idp repository contains a configuration of the [Shibboleth Identity Provider](https://wiki.shibboleth.net/confluence/display/IDP30/Home) adapted for the [Swedish eID Framework](https://github.com/swedenconnect/technical-framework). The IdP mocks the authentication phase by simply letting the user choose which user he or she wants to authenticate as. But this part is not the interesting part. The purpose is to supply actors within the Swedish eID federation with a reference implementation of how an SAML Identity Provider implements the Swedish eID Framework.

The reference implementation is built using the [Swedish eID Shibboleth base package](https://github.com/litsec/swedish-eid-shibboleth-base) which is also available as open source.

## Building

Build the IdP using Maven:

```
...swedish-eid-idp/idp> maven clean install
```

This will build the distribution `target/swedish-eid-idp-<version>.zip`. The distribution contains the following:

* **tomcat** - A directory with a stripped Tomcat instance where the war-file for the Shibboleth/Swedish eID IdP is placed under `webapps`. The [server.xml](https://github.com/elegnamnden/swedish-eid-idp/blob/master/idp/src/main/tomcat/server.xml) is also modified for the IdP purposes (HTTPS and/or AJP).
* **shibboleth** - The directory containing all Shibboleth files. This installation is a core Shibboleth distribution extended with features for the Swedish eID Framework.
* **test-credentials** - Keys and certificates for running the IdP ... In test mode that is. Of course you should have your own keys and certificates when running as a production IdP.
* **scripts** - Start-up scripts for the IdP. They are:
	- standaloneStart.sh: A start script for running the IdP in stand-alone mode using the embedded Tomcat. 
    - dockerStart.sh: A start script for starting the IdP in a Docker container. This script is added to the Docker image by the Dockerfile.
	- runStandalone.sh: An example script of how the IdP may be configured to run in stand-alone
	mode from a distribution.
	- runDevStandalone.sh: An example script of how the IdP may be configured to run in stand-alone mode directly after a build (suitable for development).
	- runDocker.sh: An example script of how to start the Docker container running the IdP.	
* **Dockerfile** - A Dockerfile for building a docker image for the reference IdP.

You can also choose to build a Docker image directly using Maven:

```
...swedish-eid-idp/idp> maven clean install dockerfile:build
```

This will create a Docker image tagged `<repo>/swedish-eid-idp:latest`. This is mainly for internal use, so it is recommended that you build your own image from the distribution (see below).

## Running the IdP

You can run the IdP either in a "stand-alone" mode where the IdP war-file is deployed on the embedded Tomcat, or you can run it in a Docker container.

In both cases the IdP needs to be configured. This is done using environment variables that are exported to the start scripts. These configuration settings are described in the [Configuration](docs/configuration.md) section.

> OK. You'll probably get the IdP up and running using the instructions below, but your SP:s also need to be able to get hold of its metadata. So, a lot of documentation will be added later on, but ... The IdP metadata will be published by a running IdP at: `https://<host:port>/idp/metadata/idp.xml`.

> Not satisfied about how the IdP metadata looks like? Check out the [metadata configuration](https://github.com/elegnamnden/swedish-eid-idp/tree/master/idp/src/main/shibboleth/config/metadata).



### Running in stand-alone mode

* Unzip the distribution.
* Create a start-up script with your own configuration (look at [runStandalone.sh](https://github.com/elegnamnden/swedish-eid-idp/blob/master/idp/scripts/runStandalone.sh) as an example).
* Run the IdP ...

> Note: You can actually run the IdP in stand-alone mode directly after you have built the IdP without bothering about the distribution ZIP. The script [runDevStandalone.sh](https://github.com/elegnamnden/swedish-eid-idp/blob/master/idp/scripts/runDevStandalone.sh) gives an example of this setup.

### Running in a Docker container

* Unzip the distribution.
* Build a Docker image:

```
> docker build . -t my-own-idp
```

* Create a start-up script your own configuration (look at [runDocker.sh](https://github.com/elegnamnden/swedish-eid-idp/blob/master/idp/scripts/runDocker.sh) as an example). It may look something like:

```
docker run -d --name my-own-idp --restart=always \
  -p 443:8443 \
  -e IDP_SERVER_HOSTNAME=idp.example.com \
  -e IDP_ENTITY_ID=http://idp.example.com \
  ... my config ...
  -v $BASE_DIR/test-credentials:/etc/swedish-eid-idp/credentials \
  -v $BASE_DIR/target/logs:/var/log/swedish-eid-idp \
  my-own-idp
```


Copyright &copy; 2016-2018, [Sweden Connect](https://swedenconnect.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
