package com.hanoi.tthc.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.hanoi.tthc.model.ReportRecord;
import com.hanoi.tthc.model.UnitRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReportHistoryService {

  private static final Logger log = LoggerFactory.getLogger(ReportHistoryService.class);
  private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  @Value("${firebase.enabled:true}")
  private boolean enabled;

  @Value("${firebase.collection:tthc_reports}")
  private String collection;

  public boolean isAvailable() {
    return enabled && !FirebaseApp.getApps().isEmpty();
  }

  public String save(String name, String reportType, List<UnitRow> rows, List<String> fileNames,
                     Map<String, Double> totals) {
    if (!isAvailable()) {
      log.warn("Storage not available – skip save");
      return null;
    }
    try {
      Firestore db = FirestoreClient.getFirestore();
      ReportRecord rec = new ReportRecord();
      rec.setName(name != null && !name.isBlank() ? name.trim() : defaultName(reportType));
      rec.setReportType(reportType);
      rec.setCreatedAt(OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(FMT));
      rec.setFileCount(fileNames != null ? fileNames.size() : 0);
      rec.setUnitCount(rows != null ? rows.size() : 0);
      rec.setFileNames(fileNames != null ? fileNames : List.of());
      rec.setTotals(totals != null ? totals : Map.of());
      rec.setRows(rows);

      Map<String, Object> data = toMap(rec);
      ApiFuture<DocumentReference> fut = db.collection(collection).add(data);
      DocumentReference ref = fut.get(45, TimeUnit.SECONDS);
      log.info("Saved report {} name={}", ref.getId(), rec.getName());
      return ref.getId();
    } catch (Exception e) {
      log.error("Save history failed: {}", e.getMessage(), e);
      return null;
    }
  }

  private String defaultName(String reportType) {
    String type = "tuan".equalsIgnoreCase(reportType) ? "Tuần" : "Kỳ";
    String ts = OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    return "Báo cáo " + type + " — " + ts;
  }

  public boolean updateName(String id, String newName) {
    if (!isAvailable() || id == null || newName == null || newName.isBlank()) return false;
    try {
      Firestore db = FirestoreClient.getFirestore();
      db.collection(collection).document(id)
          .update("name", newName.trim())
          .get(15, TimeUnit.SECONDS);
      return true;
    } catch (Exception e) {
      log.error("Update name {} failed: {}", id, e.getMessage(), e);
      return false;
    }
  }

  public boolean delete(String id) {
    if (!isAvailable() || id == null) return false;
    try {
      Firestore db = FirestoreClient.getFirestore();
      db.collection(collection).document(id).delete().get(15, TimeUnit.SECONDS);
      log.info("Deleted report {}", id);
      return true;
    } catch (Exception e) {
      log.error("Delete {} failed: {}", id, e.getMessage(), e);
      return false;
    }
  }

  public List<ReportRecord> listRecent(int limit) {
    if (!isAvailable()) return List.of();
    try {
      Firestore db = FirestoreClient.getFirestore();
      ApiFuture<QuerySnapshot> fut = db.collection(collection)
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .limit(limit)
          .get();
      List<ReportRecord> list = new ArrayList<>();
      for (DocumentSnapshot doc : fut.get(30, TimeUnit.SECONDS).getDocuments()) {
        ReportRecord r = fromDoc(doc, false); // list: không lấy rows
        list.add(r);
      }
      return list;
    } catch (Exception e) {
      log.error("List history failed: {}", e.getMessage(), e);
      return List.of();
    }
  }

  public ReportRecord get(String id) {
    if (!isAvailable() || id == null) return null;
    try {
      Firestore db = FirestoreClient.getFirestore();
      DocumentSnapshot doc = db.collection(collection).document(id).get().get(30, TimeUnit.SECONDS);
      if (!doc.exists()) return null;
      return fromDoc(doc, true);
    } catch (Exception e) {
      log.error("Get report {} failed: {}", id, e.getMessage(), e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private ReportRecord fromDoc(DocumentSnapshot doc, boolean includeRows) {
    ReportRecord r = new ReportRecord();
    r.setId(doc.getId());
    r.setName(doc.getString("name"));
    r.setReportType(doc.getString("reportType"));
    r.setCreatedAt(doc.getString("createdAt"));
    Long fc = doc.getLong("fileCount");
    r.setFileCount(fc != null ? fc.intValue() : 0);
    Long uc = doc.getLong("unitCount");
    r.setUnitCount(uc != null ? uc.intValue() : 0);
    Object fn = doc.get("fileNames");
    if (fn instanceof List) r.setFileNames((List<String>) fn);
    Object tot = doc.get("totals");
    if (tot instanceof Map) {
      Map<String, Double> totals = new LinkedHashMap<>();
      ((Map<String, Object>) tot).forEach((k, v) -> {
        if (v instanceof Number) totals.put(k, ((Number) v).doubleValue());
      });
      r.setTotals(totals);
    }
    if (includeRows) {
      Object rowsObj = doc.get("rows");
      if (rowsObj instanceof List) {
        List<UnitRow> rows = new ArrayList<>();
        for (Object o : (List<?>) rowsObj) {
          if (o instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) o;
            UnitRow ur = new UnitRow();
            ur.setStt(Objects.toString(m.get("stt"), null));
            ur.setUnit(Objects.toString(m.get("unit"), null));
            Object nums = m.get("numbers");
            if (nums instanceof Map) {
              ((Map<String, Object>) nums).forEach((k, v) -> {
                if (v instanceof Number) ur.put(k, ((Number) v).doubleValue());
              });
            }
            Object src = m.get("sources");
            if (src instanceof List) {
              ur.getSources().addAll(((List<?>) src).stream().map(Object::toString).collect(Collectors.toList()));
            } else if (src instanceof String) {
              for (String p : ((String) src).split(";")) {
                if (!p.isBlank()) ur.getSources().add(p.trim());
              }
            }
            rows.add(ur);
          }
        }
        r.setRows(rows);
      }
    }
    return r;
  }

  private Map<String, Object> toMap(ReportRecord rec) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("name", rec.getName());
    m.put("reportType", rec.getReportType());
    m.put("createdAt", rec.getCreatedAt());
    m.put("fileCount", rec.getFileCount());
    m.put("unitCount", rec.getUnitCount());
    m.put("fileNames", rec.getFileNames());
    m.put("totals", rec.getTotals());
    if (rec.getRows() != null) {
      List<Map<String, Object>> rowMaps = new ArrayList<>();
      for (UnitRow ur : rec.getRows()) {
        Map<String, Object> rm = new LinkedHashMap<>();
        rm.put("stt", ur.getStt());
        rm.put("unit", ur.getUnit());
        rm.put("numbers", ur.getNumbers());
        rm.put("sources", new ArrayList<>(ur.getSources()));
        rowMaps.add(rm);
      }
      m.put("rows", rowMaps);
    }
    return m;
  }
}
