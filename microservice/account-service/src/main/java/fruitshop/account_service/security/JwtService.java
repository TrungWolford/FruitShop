package fruitshop.account_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ROLES_CLAIM = "roles";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    public String generateAccessToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpirationMs);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claims(Map.of(
                        TOKEN_TYPE_CLAIM, "access",
                        ROLES_CLAIM, roles
                ))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(TOKEN_TYPE_CLAIM, "refresh")
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        Object tokenType = claims.get(TOKEN_TYPE_CLAIM);
        return "refresh".equals(tokenType);
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception ex) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
