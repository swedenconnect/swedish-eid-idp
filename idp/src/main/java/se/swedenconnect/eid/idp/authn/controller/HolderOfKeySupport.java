/*
 * Copyright 2022 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.eid.idp.authn.controller;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Optional;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.util.StringUtils;

import se.swedenconnect.eid.idp.authn.model.SimulatedUser;

/**
 * Support methods for Holder-of-key.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public class HolderOfKeySupport {

  public static SimulatedUser parseCertificate(final X509Certificate cert) throws CertificateEncodingException {
    final X500Name dn = new JcaX509CertificateHolder(cert).getSubject();

    final String personalIdentityNumber = getRDN(BCStyle.SERIALNUMBER, dn, false);
    final String givenName = getRDN(BCStyle.GIVENNAME, dn, true);
    final String surname = getRDN(BCStyle.SURNAME, dn, true);
    final String displayName = getRDN(BCStyle.CN, dn, false);

    // TODO: Assert that all info is there ...

    return new SimulatedUser(personalIdentityNumber, givenName, surname, displayName);
  }

  private static String getRDN(final ASN1ObjectIdentifier oid, final X500Name dn, final boolean append) {
    final RDN[] rdns = dn.getRDNs(oid);
    if (rdns.length >= 1) {
      if (!append) {
        return Optional.ofNullable(rdns[0].getFirst())
          .map(v -> IETFUtils.valueToString(v.getValue()))
          .orElse(null);
      }
      else {
        final StringBuffer sb = new StringBuffer();
        for (final AttributeTypeAndValue atv : rdns[0].getTypesAndValues()) {
          final String v = IETFUtils.valueToString(atv.getValue());
          if (StringUtils.hasText(v)) {
            if (sb.length() > 0) {
              sb.append(" ");
            }
            sb.append(v);
          }
        }
        return sb.toString();
      }
    }
    return null;
  }
  
  private HolderOfKeySupport() {
  }

}
