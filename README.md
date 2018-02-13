![Logo](https://raw.githubusercontent.com/elegnamnden/technical-framework/master/img/eln-logo.png)

# swedish-eid-idp

Reference Identity Provider for the Swedish eID Framework

---

The swedish-eid-idp repository contains a configuration of the [Shibboleth Identity Provider](https://wiki.shibboleth.net/confluence/display/IDP30/Home) adapted for the [Swedish eID Framework](https://github.com/elegnamnden/technical-framework). The IdP mocks the authentication phase by simply letting the user choose which user he or she wants to authenticate as. But this part is not the interesting part. The purpose is to supply actors within the Swedish eID federation with a reference implementation of how an SAML Identity Provider implements the Swedish eID Framework.

> Documentation about how to build, configure and deploy will be added.

The reference implementation is built using the [Swedish eID Shibboleth base package](https://github.com/litsec/swedish-eid-shibboleth-base) which is also available as open source.


Copyright &copy; 2016-2018, [E-legitimationsnämnden](https://www.elegnamnden.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).