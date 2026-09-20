package com.hanoi.tthc.controller;

import com.hanoi.tthc.dto.MergeRequest;
import com.hanoi.tthc.dto.MergeResponse;
import com.hanoi.tthc.model.ParsedFile;
import com.hanoi.tthc.model.ReportRecord;
import com.hanoi.tthc.model.UnitRow;
import com.hanoi.tthc.service.AggregateService;
import com.hanoi.tthc.service.ExcelExportService;
import com.hanoi.tthc.service.ExcelParseService;
import com.hanoi.tthc.service.ReportHistoryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

  private final ExcelParseService parseService;
  private final AggregateService aggregateService;
  private final ExcelExportService exportService;
  private final ReportHistoryService historyService;

  public ReportController(ExcelParseService parseService,
                          AggregateService aggregateService,
                          ExcelExportService exportService,
                          ReportHistoryService historyService) {
    this.parseService = parseService;
    this.aggregateService = aggregateService;
    this.exportService = exportService;
    this.historyService = historyService;
  }

  @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public List<ParsedFile> parse(@RequestParam("files") MultipartFile[] files) {
    return parseService.parseAll(files);
  }

  @PostMapping("/merge")
  public MergeResponse merge(@RequestBody MergeRequest req) {
    List<UnitRow> input = req.getRows() != null ? req.getRows() : List.of();
    List<UnitRow> rows = aggregateService.aggregate(input);
    Map<String, Double> totals = aggregateService.totals(rows);

    MergeResponse resp = new MergeResponse();
    resp.setRows(rows);
    resp.setTotals(totals);
    resp.setUnitCount(rows.size());

    if (req.isSave()) {
      String id = historyService.save(
          req.getName(),
          req.getReportType(),
          rows,
          req.getFileNames(),
          totals
      );
      resp.setSavedId(id);
    }
    return resp;
  }

  @PostMapping(value = "/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  public ResponseEntity<byte[]> export(@RequestBody MergeRequest req) throws Exception {
    List<UnitRow> rows = aggregateService.aggregate(req.getRows() != null ? req.getRows() : List.of());
    Map<String, Double> totals = aggregateService.totals(rows);
    byte[] data = exportService.export(rows, req.getReportType(), totals);
    String typeLabel = "tuan".equalsIgnoreCase(req.getReportType()) ? "Tuan" : "Ky";
    String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
    String filename = "Tong-hop-bao-cao-" + typeLabel + "-" + ts + ".xlsx";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(data);
  }

  @GetMapping("/history")
  public List<ReportRecord> history(@RequestParam(defaultValue = "50") int limit) {
    return historyService.listRecent(limit);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReportRecord> get(@PathVariable String id) {
    ReportRecord r = historyService.get(id);
    if (r == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(r);
  }

  @PatchMapping("/{id}/name")
  public ResponseEntity<Map<String, Object>> rename(@PathVariable String id,
                                                    @RequestBody Map<String, String> body) {
    String name = body != null ? body.get("name") : null;
    boolean ok = historyService.updateName(id, name);
    if (!ok) return ResponseEntity.badRequest().body(Map.of("ok", false));
    return ResponseEntity.ok(Map.of("ok", true, "name", name != null ? name.trim() : ""));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> delete(@PathVariable String id) {
    boolean ok = historyService.delete(id);
    if (!ok) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(Map.of("ok", true));
  }

  @GetMapping(value = "/{id}/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  public ResponseEntity<byte[]> downloadHistory(@PathVariable String id,
                                                @RequestParam(defaultValue = "ky") String type) throws Exception {
    ReportRecord r = historyService.get(id);
    if (r == null || r.getRows() == null) return ResponseEntity.notFound().build();
    String reportType = type != null ? type : r.getReportType();
    Map<String, Double> totals = r.getTotals() != null ? r.getTotals() : aggregateService.totals(r.getRows());
    byte[] data = exportService.export(r.getRows(), reportType, totals);
    String filename = "Tong-hop-bao-cao-" + id + ".xlsx";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(data);
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of(
        "ok", true,
        "storage", historyService.isAvailable(),
        "build", ExcelParseService.BUILD_ID
    );
  }
}
