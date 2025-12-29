package com.ecommerce.bffadmin.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemUtils {
    public static KeyPair readKeyPair(String privatePemPath, String publicPemPath) throws Exception {
        try (InputStream privIn = new ClassPathResource(privatePemPath).getInputStream();
             InputStream pubIn  = new ClassPathResource(publicPemPath).getInputStream()) {

            String privPem = new String(privIn.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            String pubPem = new String(pubIn.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");

            byte[] privBytes = Base64.getDecoder().decode(privPem);
            byte[] pubBytes  = Base64.getDecoder().decode(pubPem);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
            PublicKey pub  = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
            return new KeyPair(pub, priv);
        }
    }
}

