package com.payment.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.payment.exception.CreateTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    public String generateToken(Long merchantId) {
        Date expirationDate = new Date(System.currentTimeMillis() + jwtExpiration);
        try {
            Date now = new Date();
            JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();
            claimsSet.issueTime(now);
            claimsSet.expirationTime(expirationDate);
            claimsSet.notBeforeTime(now);
            claimsSet.claim("merchantId", merchantId);
            JWSSigner signer = new MACSigner(jwtSecret);
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet.build());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new CreateTokenException(e.getMessage());
        }
    }
}


