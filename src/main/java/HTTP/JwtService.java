package HTTP;

import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lab04.entities.User;


public class JwtService {
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long ttlMillis = 10000;

    public static String generateToken(final User user) {

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        long expMillis = nowMillis + ttlMillis;
        Date expiry = new Date(expMillis);

        return Jwts.builder()
                .setSubject(user.getLogin())
                .signWith(SECRET_KEY)
                .setIssuedAt(now)
                .claim("role", user.getRole())
                .setExpiration(expiry)
                .compact();

    }

    public static String getUsernameFromToken(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }

}
