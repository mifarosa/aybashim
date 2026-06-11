package com.aybashim.backend.service;

import com.aybashim.backend.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final Pattern SUBJECT_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*(\\d+)");
    private static final Pattern EXPIRATION_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    @Value("${app.jwt.secret:change-this-secret-before-production}")
    private String secret;

    @Value("${app.jwt.expiration-hours:24}")
    private long expirationHours;

    public String generate(AppUser user) {
        long expiresAt = Instant.now().plusSeconds(expirationHours * 3600).getEpochSecond();

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":" + user.getId() +
                ",\"email\":\"" + escape(user.getEmail()) +
                "\",\"exp\":" + expiresAt + "}";

        return sign(header, payload);
    }

    public Long validateAndGetUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String unsigned = parts[0] + "." + parts[1];
            if (!slowEquals(sign(unsigned), parts[2])) {
                return null;
            }

            String payload = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);

            Long expiresAt = extractLong(payload, EXPIRATION_PATTERN);
            if (expiresAt == null || expiresAt < Instant.now().getEpochSecond()) {
                return null;
            }

            return extractLong(payload, SUBJECT_PATTERN);
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String header, String payload) {
        String encodedHeader = URL_ENCODER.encodeToString(header.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = URL_ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String unsigned = encodedHeader + "." + encodedPayload;
        return unsigned + "." + sign(unsigned);
    }

    private String sign(String unsigned) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Token signing failed", e);
        }
    }

    private Long extractLong(String payload, Pattern pattern) {
        Matcher matcher = pattern.matcher(payload);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean slowEquals(String a, String b) {
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        int diff = left.length ^ right.length;
        for (int i = 0; i < left.length && i < right.length; i++) {
            diff |= left[i] ^ right[i];
        }
        return diff == 0;
    }
}
