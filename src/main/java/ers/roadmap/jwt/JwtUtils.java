package ers.roadmap.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final int jwtExpireMillis;

    public JwtUtils(@Value("${jwt.secret}") String secret, @Value("${jwt.expire}") int jwtExpireMillis) {
        this.secretKey =  getKey(secret);
        this.jwtExpireMillis = jwtExpireMillis;
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpireMillis))
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try{
            System.out.println("Validating!");
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        }catch (MalformedJwtException e) {
            System.out.println("Invalid JWT Token {} " + e.getMessage());
        }catch (ExpiredJwtException e) {
            System.out.println("JWT Token is expired {} " + e.getMessage());
        }catch (UnsupportedJwtException e) {
            System.out.println("JWT Token is unsupported {} " + e.getMessage());
        }catch (IllegalArgumentException e) {
            System.out.println("JWT Claims String is empty" + e.getMessage());
        }catch (SignatureException e) {
            System.out.println("JWT is made up " + e.getMessage());
        }
        return false;
    }

    private SecretKey getKey(String secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

}