package com.example.dividend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1시간
    private static final String KEY_ROLES = "roles";

    @Value("${spring.jwt.secret}")
    private String secretKey;

    /**
     * 토큰 생성(발급)
     */
    public String generateToken(String username, List<String> roles) {
        // 사용자 권한 정보 저장
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(KEY_ROLES, roles);

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);   // 유효시간

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)           // 토큰 생성 시간
                .setExpiration(expireDate)  // 토큰 시간 만료
                .signWith(SignatureAlgorithm.HS512, this.secretKey) // 사용할 알고리즘, 비밀키
                .compact();
    }

    public String getUsername(String token) {
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        Claims claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date()); // 만료시간을 현재시간과 비교
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
