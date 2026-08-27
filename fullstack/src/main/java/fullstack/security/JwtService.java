package fullstack.security;

import fullstack.model.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;

@Service
public class JwtService {

    private final SecretKey
        signingKey;

    private final long
        expirationMs;

    public JwtService(
            @Value(
                "${app.jwt.secret}"
            )
            String secret,

            @Value(
                "${app.jwt.expiration-ms}"
            )
            long expirationMs) {

        this.signingKey =
            Keys.hmacShaKeyFor(
                Decoders.BASE64
                    .decode(secret)
            );

        this.expirationMs =
            expirationMs;
    }

    /*
     * =====================================================
     * GENERATE TOKEN
     * =====================================================
     */

    public String generateToken(
            AppUser user) {

        Date now =
            new Date();

        Date expiration =
            new Date(
                now.getTime()
                + expirationMs
            );

        return Jwts
            .builder()
            .subject(
                user.getUsername()
            )
            .claim(
                "userId",
                user.getId()
            )
            .claim(
                "name",
                user.getName()
            )
            .claim(
                "role",
                user.getRole().name()
            )
            .issuedAt(now)
            .expiration(expiration)
            .signWith(
                signingKey
            )
            .compact();
    }

    /*
     * =====================================================
     * EXTRACT USERNAME
     * =====================================================
     */

    public String extractUsername(
            String token) {

        return extractClaims(
            token
        ).getSubject();
    }

    /*
     * =====================================================
     * VALIDATE
     * =====================================================
     */

    public boolean isTokenValid(
            String token,
            AppUser user) {

        Claims claims =
            extractClaims(token);

        String username =
            claims.getSubject();

        Date expiration =
            claims.getExpiration();

        return username.equalsIgnoreCase(
                    user.getUsername()
               )
               &&
               expiration.after(
                   new Date()
               );
    }

    /*
     * =====================================================
     * PARSE CLAIMS
     * =====================================================
     */

    private Claims extractClaims(
            String token) {

        return Jwts
            .parser()
            .verifyWith(
                signingKey
            )
            .build()
            .parseSignedClaims(
                token
            )
            .getPayload();
    }

    public long getExpirationMs() {

        return expirationMs;
    }
}