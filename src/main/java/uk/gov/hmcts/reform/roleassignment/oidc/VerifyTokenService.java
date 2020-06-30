/*
package uk.gov.hmcts.reform.roleassignment.oidc;

import java.net.URL;
import java.security.Key;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.jwk.AsymmetricJWK;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.SecretJWK;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;

@Component
public class VerifyTokenService {

    private final JWSVerifierFactory jwsVerifierFactory;

    public VerifyTokenService(JWSVerifierFactory jwsVerifierFactory) {
        this.jwsVerifierFactory = jwsVerifierFactory;
    }

    public boolean verifyTokenSignature(String token, OAuth2AuthorizedClient oauth2Client) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);

            JWKSet jsonWebKeySet = loadJsonWebKeySet(oauth2Client.getClientRegistration().getProviderDetails()
                                                                 .getJwkSetUri());

            JWSHeader jwsHeader = signedJwt.getHeader();
            Key key = findKeyById(jsonWebKeySet, jwsHeader.getKeyID());

            JWSVerifier jwsVerifier = jwsVerifierFactory.createJWSVerifier(jwsHeader, key);

            return signedJwt.verify(jwsVerifier);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private JWKSet loadJsonWebKeySet(String jwksUrl) {
        try {
            return JWKSet.load(new URL(jwksUrl));
        } catch (Exception e) {
            throw new RuntimeException("JWKS error", e);
        }
    }

    private Key findKeyById(JWKSet jsonWebKeySet, String keyId) {
        try {
            JWK jsonWebKey = jsonWebKeySet.getKeyByKeyId(keyId);
            if (jsonWebKey == null) {
                throw new RuntimeException("JWK does not exist in the key set");
            }
            if (jsonWebKey instanceof SecretJWK) {
                return ((SecretJWK) jsonWebKey).toSecretKey();
            }
            if (jsonWebKey instanceof AsymmetricJWK) {
                return ((AsymmetricJWK) jsonWebKey).toPublicKey();
            }
            throw new RuntimeException("Unsupported JWK " + jsonWebKey.getClass().getName());
        } catch (JOSEException e) {
            throw new RuntimeException("Invalid JWK", e);
        }
    }
}
*/
