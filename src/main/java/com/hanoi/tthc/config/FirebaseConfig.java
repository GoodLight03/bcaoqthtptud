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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FirebaseConfig {

  @Value("${firebase.credentials:}") // có thể để trống
  private String firebaseCredentials;

  @PostConstruct
  public void init() throws Exception {
    InputStream serviceAccount = null;

    // Ưu tiên 1: Lấy từ biến môi trường (JSON content)
    String credentialsJson = System.getenv("FIREBASE_CREDENTIALS");
    if (credentialsJson != null && !credentialsJson.isBlank()) {
      serviceAccount = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
      System.out.println("Đã load Firebase credentials từ biến môi trường FIREBASE_CREDENTIALS");
    }
    // Ưu tiên 2: Lấy từ file ngoài (khi mount volume)
    else if (Files.exists(Paths.get("/secrets/firebase-service-account.json"))) {
      serviceAccount = new FileInputStream("/secrets/firebase-service-account.json");
      System.out.println("Đã load Firebase credentials từ /secrets/...");
    }
    // Ưu tiên 3: classpath (chỉ dùng local)
    else {
      serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-service-account.json");
      if (serviceAccount == null) {
        throw new RuntimeException("KHÔNG TÌM THẤY file credentials Firebase");
      }
      System.out.println("Đã load Firebase credentials từ classpath (local)");
    }

    FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

    if (FirebaseApp.getApps().isEmpty()) {
      FirebaseApp.initializeApp(options);
    }
  }
}

//@Configuration
//public class FirebaseConfig {
//
//  private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);
//
//  @Value("${firebase.enabled:true}")
//  private boolean enabled;
//
//  @Value("${firebase.credentials-path:firebase-service-account.json}")
//  private String credentialsPath;
//
//  @Value("${firebase.storage-bucket:}")
//  private String storageBucket;
//
//  @PostConstruct
//  public void init() {
//    if (!enabled) {
//      log.warn("Firebase DISABLED (firebase.enabled=false). History will not be saved.");
//      return;
//    }
//    try {
//      if (!FirebaseApp.getApps().isEmpty()) {
//        return;
//      }
//      ClassPathResource resource = new ClassPathResource(credentialsPath);
//      if (!resource.exists()) {
//        log.error("KHÔNG TÌM THẤY file credentials: classpath:{}", credentialsPath);
//        log.error("Hãy đặt file firebase-service-account.json vào src/main/resources/");
//        return;
//      }
//      try (InputStream in = resource.getInputStream()) {
//        FirebaseOptions.Builder builder = FirebaseOptions.builder()
//            .setCredentials(GoogleCredentials.fromStream(in));
//        if (storageBucket != null && !storageBucket.isBlank()
//            && !storageBucket.startsWith("YOUR_")) {
//          builder.setStorageBucket(storageBucket);
//        }
//        FirebaseApp.initializeApp(builder.build());
//        log.info("Firebase initialized OK");
//      }
//    } catch (Exception e) {
//      log.error("Firebase init failed: {}", e.getMessage(), e);
//    }
//  }
//}


