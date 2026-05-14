package br.com.ford.specradar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String token;
    private String tipo;
    private LocalDateTime expiraEm;

    public static TokenResponse of(String token, long expirationMs) {
        return TokenResponse.builder()
                .token(token)
                .tipo("Bearer")
                .expiraEm(LocalDateTime.now().plusSeconds(expirationMs / 1000))
                .build();
    }
}