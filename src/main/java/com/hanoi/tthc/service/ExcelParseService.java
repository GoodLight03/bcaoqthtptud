package com.hanoi.tthc.service;

import com.hanoi.tthc.model.CanonField;
import com.hanoi.tthc.model.ParsedFile;
import com.hanoi.tthc.model.UnitRow;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * Port sát logic parse từ index.html (SheetJS):
 * - A: Tổng hợp theo đơn vị
 * - B: II.06a/VPCP
 * - C: 07a/BTP  (file .xls thường có cột trống đầu: STT ở cột 1, tên ĐV ở cột 2)
 */
@Service
public class ExcelParseService {

  public static final String BUILD_ID = "2026-09-20-FINAL-07a";

  private final UnitNormalizeService unitNormalize;

  public ExcelParseService(UnitNormalizeService unitNormalize) {
    this.unitNormalize = unitNormalize;
  }

  public List<ParsedFile> parseAll(MultipartFile[] files) {
    List<ParsedFile> result = new ArrayList<>();
    if (files == null) return result;
    for (MultipartFile f : files) {
      if (f == null || f.isEmpty()) continue;
      String name = f.getOriginalFilename() != null ? f.getOriginalFilename() : "unknown.xlsx";
      try (InputStream in = f.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
        result.add(parseWorkbook(name, wb));
      } catch (Exception e) {
        ParsedFile err = new ParsedFile();
        err.setName(name);
        err.setFamilyCode("unknown");
        err.setFamilyLabel("Lỗi");
        err.setError(e.getMessage());
        result.add(err);
      }
    }
    return result;
  }

  private ParsedFile parseWorkbook(String name, Workbook wb) {
    Sheet bestSheet = null;
    int bestScore = -1;
    String bestName = "";
    for (int i = 0; i < wb.getNumberOfSheets(); i++) {
      Sheet sh = wb.getSheetAt(i);
      List<List<Object>> aoa = sheetToAoa(sh, 40);
      int sc = scoreSheet(aoa);
      if (sc > bestScore) {
        bestScore = sc;
        bestSheet = sh;
        bestName = sh.getSheetName();
      }
    }
    if (bestSheet == null || bestScore < 5) {
      ParsedFile pf = new ParsedFile();
      pf.setName(name);
      pf.setSheetName(bestName);
      pf.setFamilyCode("unknown");
      pf.setFamilyLabel("Không chắc");
      pf.setError("Không nhận ra bảng TTHC");
      return pf;
    }
    List<List<Object>> aoa = sheetToAoa(bestSheet, 8000);
    if (isTongHopTheoDonVi(aoa)) {
      return parseTongHopTheoDonVi(name, bestName, aoa);
    }
    return parseGeneric(name, bestName, aoa);
  }

  private List<List<Object>> sheetToAoa(Sheet sheet, int maxRows) {
    List<List<Object>> aoa = new ArrayList<>();
    int last = Math.min(sheet.getLastRowNum(), maxRows - 1);
    int maxCol = 0;
    for (int r = 0; r <= last; r++) {
      Row row = sheet.getRow(r);
      if (row != null && row.getLastCellNum() > maxCol) maxCol = row.getLastCellNum();
    }
    if (maxCol < 3) maxCol = 3;
    for (int r = 0; r <= last; r++) {
      Row row = sheet.getRow(r);
      List<Object> cells = new ArrayList<>(maxCol);
      for (int c = 0; c < maxCol; c++) {
        cells.add(row == null ? null : getCellRaw(row.getCell(c)));
      }
      aoa.add(cells);
    }
    return aoa;
  }

  private Object getCellRaw(Cell cell) {
    if (cell == null) return null;
    CellType type = cell.getCellType();
    if (type == CellType.FORMULA) {
      try {
        type = cell.getCachedFormulaResultType();
      } catch (Exception e) {
        return null;
      }
    }
    switch (type) {
      case BLANK:
        return null;
      case NUMERIC:
        return cell.getNumericCellValue();
      case STRING: {
        String s = cell.getStringCellValue();
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
      }
      case BOOLEAN:
        return cell.getBooleanCellValue() ? 1.0 : 0.0;
      default:
        return null;
    }
  }

  private String norm(Object v) {
    if (v == null) return "";
    if (v instanceof Number) {
      double d = ((Number) v).doubleValue();
      if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
      return String.valueOf(d);
    }
    return String.valueOf(v).trim();
  }

  private String normLower(Object v) {
    return norm(v).toLowerCase();
  }

  private Double toNum(Object v) {
    if (v == null) return null;
    if (v instanceof Number) {
      double d = ((Number) v).doubleValue();
      if (Double.isNaN(d) || Double.isInfinite(d)) return null;
      return d;
    }
    String s = String.valueOf(v).trim();
    if (s.isEmpty()) return null;
    if (s.matches("^\\d{1,3}(\\.\\d{3})+(,\\d+)?$")) {
      s = s.replace(".", "").replace(",", ".");
    } else {
      s = s.replaceAll("\\s", "").replace(",", ".");
    }
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private boolean isDataStt(Object v) {
    return norm(v).matches("\\d+(\\.\\d+)*");
  }

  private int scoreSheet(List<List<Object>> aoa) {
    StringBuilder head = new StringBuilder();
    for (int i = 0; i < Math.min(12, aoa.size()); i++) {
      for (Object c : aoa.get(i)) head.append(normLower(c)).append(' ');
      head.append(" | ");
    }
    String h = head.toString();
    int s = 0;
    if (h.contains("stt")) s += 5;
    if (h.contains("đơn vị") || h.contains("don vi")) s += 4;
    if (h.contains("tiếp nhận") || h.contains("tiep nhan")) s += 3;
    if (h.contains("giải quyết") || h.contains("giai quyet")) s += 3;
    if (h.contains("tthc")) s += 2;
    return s;
  }

  private static class HeaderPos {
    int row = -1;
    int sttCol = 0;
  }

  /** Tìm dòng + cột chứa chữ STT (07a hay lệch 1 cột trống đầu). */
  private HeaderPos findHeaderPos(List<List<Object>> aoa) {
    HeaderPos pos = new HeaderPos();
    for (int i = 0; i < Math.min(aoa.size(), 40); i++) {
      List<Object> row = aoa.get(i);
      for (int c = 0; c < Math.min(row.size(), 8); c++) {
        if (normLower(row.get(c)).equals("stt")) {
          pos.row = i;
          pos.sttCol = c;
          return pos;
        }
      }
    }
    return pos;
  }

  private boolean isTongHopTheoDonVi(List<List<Object>> aoa) {
    StringBuilder t = new StringBuilder();
    for (int i = 0; i < Math.min(12, aoa.size()); i++) {
      for (Object c : aoa.get(i)) t.append(normLower(c)).append(' ');
      t.append(" | ");
    }
    String s = t.toString();
    return s.contains("tổng hợp kết quả giải quyết tthc theo đơn vị")
        || s.contains("tong hop ket qua giai quyet tthc theo don vi")
        || (s.contains("tổng hợp kết quả giải quyết tthc") && s.contains("theo đơn vị"));
  }

  private String detectFamily(List<List<Object>> aoa) {
    if (isTongHopTheoDonVi(aoa)) return "A";
    StringBuilder t = new StringBuilder();
    for (int i = 0; i < Math.min(12, aoa.size()); i++) {
      for (Object c : aoa.get(i)) t.append(normLower(c)).append(' ');
      t.append('\n');
    }
    String s = t.toString();
    if (s.contains("07a/btp") || (s.contains("07a") && s.contains("btp")) || s.contains("biểu số 07a")) return "C";
    if (s.contains("ii.06a") || s.contains("vpcp") || s.contains("tình hình, kết quả giải quyết thủ tục")) return "B";
    return "unknown";
  }

  private boolean isFormulaHeaderRow(List<Object> row) {
    int h = 0;
    if (row == null) return false;
    for (Object c : row) {
      if (norm(c).matches("\\(\\d+\\)")) h++;
    }
    return h >= 3;
  }

  private List<String> forwardFill(List<Object> row, int width) {
    List<String> out = new ArrayList<>(width);
    String last = "";
    for (int c = 0; c < width; c++) {
      String v = c < row.size() ? norm(row.get(c)) : "";
      if (!v.isEmpty()) {
        last = v;
        out.add(v);
      } else {
        out.add(last);
      }
    }
    return out;
  }

  private static class HeaderInfo {
    List<String> labels;
    int dataStart;
    int sttCol;
    int unitCol;
  }

  private HeaderInfo buildLabels(List<List<Object>> aoa, HeaderPos pos) {
    int headerStart = pos.row;
    int sttCol = pos.sttCol;
    int width = 3;
    for (int i = headerStart; i < Math.min(headerStart + 8, aoa.size()); i++) {
      width = Math.max(width, aoa.get(i).size());
    }
    List<List<String>> headerRows = new ArrayList<>();
    int dataStart = headerStart + 1;
    for (int i = headerStart; i < Math.min(headerStart + 8, aoa.size()); i++) {
      List<Object> row = aoa.get(i);
      if (isFormulaHeaderRow(row)) {
        dataStart = i + 1;
        continue;
      }
      Object sttCell = sttCol < row.size() ? row.get(sttCol) : null;
      if (i > headerStart && isDataStt(sttCell)) {
        dataStart = i;
        break;
      }
      headerRows.add(forwardFill(row, width));
      dataStart = i + 1;
    }
    List<String> labels = new ArrayList<>();
    for (int c = 0; c < width; c++) {
      List<String> parts = new ArrayList<>();
      for (List<String> hr : headerRows) {
        String x = c < hr.size() ? hr.get(c) : "";
        if (!x.isEmpty() && !parts.contains(x)) parts.add(x);
      }
      labels.add(String.join(" > ", parts));
    }
    HeaderInfo hi = new HeaderInfo();
    hi.labels = labels;
    hi.dataStart = dataStart;
    hi.sttCol = sttCol;
    hi.unitCol = sttCol + 1;
    return hi;
  }

  /** Phân loại cột theo NHÃN — không hard-code cột 0/1 (07a lệch 1 cột). */
  private String classify(String label, int colIndex) {
    String L = label == null ? "" : label.toLowerCase();
    if (L.equals("stt") || L.startsWith("stt >") || L.endsWith(" > stt") || L.matches("stt(\\s|>).*")) {
      return "stt";
    }
    if (L.contains("đơn vị tính") || L.contains("don vi tinh")) {
      /* tiêu đề, không phải cột đơn vị */
    } else if (L.contains("đơn vị thực hiện") || L.contains("don vi thuc hien")
        || L.contains("đơn vị giải quyết") || L.contains("don vi giai quyet")
        || L.contains("đơn vị") || L.contains("don vi")) {
      return "unit";
    }
    if (L.contains("không được tiếp nhận")) return "rejected";
    if (L.contains("chờ tiếp nhận") && L.contains("đúng")) return "pending_ontime";
    if (L.contains("chờ tiếp nhận") && L.contains("quá")) return "pending_late";
    if (L.contains("chờ tiếp nhận")) return "pending_receive";
    if (L.contains("dừng xử lý")) return "stopped";
    if (L.contains("rút") && !L.contains("trước")) return "withdrawn";
    if (L.contains("thanh toán")) {
      if (L.contains("tổng")) return "pay_total";
      if (L.contains("trực tuyến")) return "pay_online";
      if (L.contains("trực tiếp")) return "pay_direct";
    }
    if (L.contains("phi địa giới")) return "recv_non_local";
    if (L.contains("hình thức khác")) return "recv_online_other";
    if (L.contains("thư xin lỗi") && L.contains("đã") && (L.contains("đã giải quyết") || L.contains("quá hạn")))
      return "res_late_sorry";
    if (L.contains("thư xin lỗi") && L.contains("chưa")) return "res_late_no_sorry";

    String section = null;
    if (L.contains("đã giải quyết") || L.contains("da giai quyet")) section = "res";
    else if (L.contains("đang giải quyết") || L.contains("dang giai quyet")) section = "proc";
    else if (L.contains("tiếp nhận") || L.contains("tiep nhan") || L.contains("nhận giải quyết") || L.contains("hồ sơ nhận"))
      section = "recv";

    if ("recv".equals(section)) {
      if (L.contains("tổng số") || L.contains("tong so")) return "recv_total";
      if (L.contains("kỳ trước") || L.contains("chuyển qua") || L.contains("từ kỳ trước")) return "recv_carry";
      if (L.contains("toàn trình")) return "recv_online_full";
      if (L.contains("một phần")) return "recv_online_partial";
      if (L.contains("bưu chính") || L.contains("bcci")) return "recv_postal";
      if (L.contains("trực tuyến")) return "recv_online_full";
      if (L.contains("trực tiếp")) return "recv_direct";
      return null;
    }
    if ("res".equals(section)) {
      if (L.contains("tổng số") || L.contains("tong so")) return "res_total";
      if (L.contains("quá hạn") || L.contains("qua han")) return "res_late";
      if ((L.contains("đúng") || L.contains("dung")) && (L.contains("trước") || L.contains("truoc"))) return "res_before";
      if (L.contains("trước hạn") || L.contains("truoc han")) return "res_before";
      if (L.contains("đúng hạn") || L.contains("dung han") || L.contains("đúng") || L.contains("dung")) return "res_ontime";
      return null;
    }
    if ("proc".equals(section)) {
      if (L.contains("tổng số") || L.contains("tong so")) return "proc_total";
      if (L.contains("quá hạn") || L.contains("qua han")) return "proc_late";
      if (L.contains("trong hạn") || L.contains("chưa đến hạn") || L.contains("chua den han")) return "proc_ontime";
      return null;
    }
    return null;
  }

  private int resolveUnitCol(HeaderInfo hi, Map<String, Integer> fieldToCol) {
    Integer mapped = fieldToCol.get("unit");
    if (mapped != null && mapped != hi.sttCol) return mapped;
    return hi.sttCol + 1;
  }

  private String cellAt(List<Object> row, int idx) {
    if (idx < 0 || idx >= row.size()) return "";
    return norm(row.get(idx));
  }

  /**
   * Đọc tên đơn vị: ưu tiên cột unit; nếu ô đó là số (nhầm STT) thì lấy ô chữ kế bên.
   */
  private String readUnitName(List<Object> row, int unitIdx, int sttCol) {
    String unitRaw = cellAt(row, unitIdx);
    if (!unitRaw.isEmpty() && !unitRaw.matches("\\d+(\\.\\d+)*")) return unitRaw;
    // quét từ sau cột STT: ô chữ đầu tiên (không phải số)
    for (int c = sttCol + 1; c < Math.min(row.size(), sttCol + 4); c++) {
      String v = cellAt(row, c);
      if (!v.isEmpty() && !v.matches("\\d+(\\.\\d+)*") && v.length() > 2) return v;
    }
    return unitRaw;
  }

  private Map<String, Integer> mapFields(HeaderInfo hi) {
    Map<String, Integer> fieldToCol = new LinkedHashMap<>();
    for (int i = 0; i < hi.labels.size(); i++) {
      String f = classify(hi.labels.get(i), i);
      if (f != null) fieldToCol.putIfAbsent(f, i);
    }
    fieldToCol.putIfAbsent("stt", hi.sttCol);
    fieldToCol.putIfAbsent("unit", hi.sttCol + 1);
    return fieldToCol;
  }

  private ParsedFile parseTongHopTheoDonVi(String name, String sheetName, List<List<Object>> aoa) {
    ParsedFile pf = new ParsedFile();
    pf.setName(name);
    pf.setSheetName(sheetName);
    pf.setFamilyCode("A");
    pf.setFamilyLabel("Tổng hợp theo đơn vị");

    HeaderPos pos = findHeaderPos(aoa);
    if (pos.row < 0) {
      pf.setError("Không thấy STT");
      return pf;
    }
    HeaderInfo hi = buildLabels(aoa, pos);
    Map<String, Integer> fieldToCol = mapFields(hi);
    int unitIdx = resolveUnitCol(hi, fieldToCol);

    List<String> numKeys = CanonField.numericKeys();
    List<UnitRow> rows = new ArrayList<>();
    for (int r = hi.dataStart; r < aoa.size(); r++) {
      List<Object> row = aoa.get(r);
      if (isEmptyRow(row)) continue;
      if (isFormulaHeaderRow(row)) continue;

      String sttRaw = cellAt(row, hi.sttCol);
      String unitRaw = readUnitName(row, unitIdx, hi.sttCol);
      if (unitRaw.isEmpty()) continue;
      String ul = unitRaw.toLowerCase();
      if (ul.equals("đơn vị giải quyết tthc") || ul.equals("đơn vị thực hiện")) continue;
      if (unitRaw.matches("\\(\\d+\\)")) continue;
      if (sttRaw.matches("(?i).*tổng cộng.*") || unitRaw.matches("(?i).*tổng cộng.*")) continue;
      if (ul.contains("trong đó")) continue;

      UnitRow ur = new UnitRow(unitNormalize.normalizeUnitName(unitRaw));
      ur.setStt(sttRaw);
      ur.getSources().add(name);
      for (String k : numKeys) {
        Integer idx = fieldToCol.get(k);
        if (idx != null && idx < row.size()) {
          Double v = toNum(row.get(idx));
          if (v != null) ur.put(k, v);
        }
      }
      rows.add(ur);
    }
    pf.setRows(rows);
    pf.setNumericRowCount((int) rows.stream().filter(this::rowHasNum).count());
    return pf;
  }

  private ParsedFile parseGeneric(String name, String sheetName, List<List<Object>> aoa) {
    String fam = detectFamily(aoa);
    String label = switch (fam) {
      case "C" -> "Biểu 07a/BTP";
      case "B" -> "Biểu II.06a/VPCP";
      case "A" -> "Tổng hợp theo đơn vị";
      default -> "Cấu trúc chung";
    };
    ParsedFile pf = new ParsedFile();
    pf.setName(name);
    pf.setSheetName(sheetName);
    pf.setFamilyCode(fam);
    pf.setFamilyLabel(label);

    HeaderPos pos = findHeaderPos(aoa);
    if (pos.row < 0) {
      pf.setError("Không thấy STT");
      return pf;
    }
    HeaderInfo hi = buildLabels(aoa, pos);
    Map<String, Integer> fieldToCol = mapFields(hi);
    int unitIdx = resolveUnitCol(hi, fieldToCol);

    List<String> numKeys = CanonField.numericKeys();
    List<UnitRow> rows = new ArrayList<>();
    for (int r = hi.dataStart; r < aoa.size(); r++) {
      List<Object> row = aoa.get(r);
      if (isEmptyRow(row)) continue;
      if (isFormulaHeaderRow(row)) continue;

      String sttRaw = cellAt(row, hi.sttCol);
      String unitRaw = readUnitName(row, unitIdx, hi.sttCol);
      if (sttRaw.matches("(?i).*tổng cộng.*") || unitRaw.matches("(?i).*tổng cộng.*")) continue;
      if (unitRaw.isEmpty()) continue;
      String ul = unitRaw.toLowerCase();
      if (ul.equals("đơn vị giải quyết tthc") || ul.equals("đơn vị thực hiện")) continue;
      if (unitRaw.matches("\\(\\d+\\)")) continue;

      Map<String, Double> vals = new HashMap<>();
      boolean hasNum = false;
      for (String k : numKeys) {
        Integer idx = fieldToCol.get(k);
        Double v = null;
        if (idx != null && idx < row.size()) v = toNum(row.get(idx));
        if (v != null) {
          vals.put(k, v);
          hasNum = true;
        }
      }
      if (!isDataStt(sttRaw) && !hasNum) continue;

      UnitRow ur = new UnitRow(unitNormalize.normalizeUnitName(unitRaw));
      ur.setStt(sttRaw);
      ur.getSources().add(name);
      vals.forEach(ur::put);
      rows.add(ur);
    }
    pf.setRows(rows);
    pf.setNumericRowCount((int) rows.stream().filter(this::rowHasNum).count());
    return pf;
  }

  private boolean isEmptyRow(List<Object> row) {
    if (row == null || row.isEmpty()) return true;
    for (Object c : row) {
      if (c != null && !norm(c).isEmpty()) return false;
    }
    return true;
  }

  private boolean rowHasNum(UnitRow r) {
    return CanonField.numericKeys().stream().anyMatch(k -> r.get(k) != null);
  }
}
