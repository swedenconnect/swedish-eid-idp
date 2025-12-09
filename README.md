![Logo](https://github.com/swedenconnect/technical-framework/blob/master/img/sweden-connect.png)

# swedish-eid-idp

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Reference Identity Provider for the Swedish eID Framework

---

The swedish-eid-idp repository is a SAML IdP-reference implementation of the [Swedish eID Framework](https://docs.swedenconnect.se/technical-framework/). It uses the [saml-identity-provider](https://github.com/swedenconnect/saml-identity-provider) Spring Boot Starter which is the Sweden Connect 
Spring Security SAML IdP implementation. 

The IdP mocks the authentication phase by simply letting the
user choose which user he or she wants to authenticate as. But this part is not the interesting part.
The purpose is to supply actors within the Swedish eID federation with a reference implementation of
how an SAML Identity Provider implements the Swedish eID Framework.

---

Copyright &copy; 2016-2025, [Sweden Connect](https://swedenconnect.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
