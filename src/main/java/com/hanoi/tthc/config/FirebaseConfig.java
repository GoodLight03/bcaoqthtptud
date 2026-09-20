package com.hanoi.tthc.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

  private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

  @Value("${firebase.enabled:true}")
  private boolean enabled;

  @Value("${firebase.credentials-path:firebase-service-account.json}")
  private String credentialsPath;

  @Value("${firebase.storage-bucket:}")
  private String storageBucket;

  @PostConstruct
  public void init() {
    if (!enabled) {
      log.warn("Firebase DISABLED (firebase.enabled=false). History will not be saved.");
      return;
    }
    try {
      if (!FirebaseApp.getApps().isEmpty()) {
        return;
      }
      ClassPathResource resource = new ClassPathResource(credentialsPath);
      if (!resource.exists()) {
        log.error("KHÔNG TÌM THẤY file credentials: classpath:{}", credentialsPath);
        log.error("Hãy đặt file firebase-service-account.json vào src/main/resources/");
        return;
      }
      try (InputStream in = resource.getInputStream()) {
        FirebaseOptions.Builder builder = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(in));
        if (storageBucket != null && !storageBucket.isBlank()
            && !storageBucket.startsWith("YOUR_")) {
          builder.setStorageBucket(storageBucket);
        }
        FirebaseApp.initializeApp(builder.build());
        log.info("Firebase initialized OK");
      }
    } catch (Exception e) {
      log.error("Firebase init failed: {}", e.getMessage(), e);
    }
  }
}
