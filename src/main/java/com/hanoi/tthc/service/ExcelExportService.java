package com.hanoi.tthc.service;

import com.hanoi.tthc.model.CanonField;
import com.hanoi.tthc.model.UnitRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

  public byte[] export(List<UnitRow> rows, String reportType, Map<String, Double> totals) throws Exception {
    boolean weekly = "tuan".equalsIgnoreCase(reportType);
    List<CanonField> canon = weekly ? CanonField.weekly() : CanonField.full();

    try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      Sheet sheet = wb.createSheet("Tong hop TTHC");

      XSSFCellStyle yellow = wb.createCellStyle();
      yellow.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0xFF, (byte) 0xE5, 0x66}, null));
      yellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      yellow.setAlignment(HorizontalAlignment.CENTER);
      yellow.setVerticalAlignment(VerticalAlignment.CENTER);
      yellow.setWrapText(true);
      Font bold = wb.createFont();
      bold.setBold(true);
      yellow.setFont(bold);

      XSSFCellStyle yellowSub = wb.createCellStyle();
      yellowSub.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0xFF, (byte) 0xF3, (byte) 0xB0}, null));
      yellowSub.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      yellowSub.setAlignment(HorizontalAlignment.CENTER);
      yellowSub.setVerticalAlignment(VerticalAlignment.CENTER);
      yellowSub.setWrapText(true);
      yellowSub.setFont(bold);

      CellStyle numStyle = wb.createCellStyle();
      numStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0"));

      Row h1 = sheet.createRow(0);
      Row h2 = sheet.createRow(1);
      for (int c = 0; c < canon.size(); c++) {
        CanonField f = canon.get(c);
        Cell c0 = h1.createCell(c);
        c0.setCellValue(f.getTop());
        c0.setCellStyle(yellow);
        Cell c1 = h2.createCell(c);
        c1.setCellValue(f.getSub() != null ? f.getSub() : "");
        c1.setCellStyle(yellowSub);
      }

      // merges
      int col = 0;
      while (col < canon.size()) {
        String top = canon.get(col).getTop();
        int span = 1;
        while (col + span < canon.size() && top.equals(canon.get(col + span).getTop())) span++;
        if (span == 1 && (canon.get(col).getSub() == null || canon.get(col).getSub().isEmpty())) {
          sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        } else if (span > 1) {
          sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + span - 1));
        }
        col += span;
      }

      int rowIdx = 2;
      for (UnitRow r : rows) {
        Row row = sheet.createRow(rowIdx++);
        for (int c = 0; c < canon.size(); c++) {
          CanonField f = canon.get(c);
          Cell cell = row.createCell(c);
          String key = f.getKey();
          if ("stt".equals(key)) {
            cell.setCellValue(r.getStt() != null ? r.getStt() : "");
          } else if ("unit".equals(key)) {
            cell.setCellValue(r.getUnit() != null ? r.getUnit() : "");
          } else if ("sources".equals(key)) {
            cell.setCellValue(r.sourcesJoined());
          } else {
            Double v = fieldVal(r, key, weekly);
            if (v != null && v != 0) {
              cell.setCellValue(v);
              cell.setCellStyle(numStyle);
            }
          }
        }
      }

      // total row
      Row totalRow = sheet.createRow(rowIdx);
      for (int c = 0; c < canon.size(); c++) {
        CanonField f = canon.get(c);
        Cell cell = totalRow.createCell(c);
        if ("unit".equals(f.getKey())) {
          cell.setCellValue("TỔNG CỘNG");
        } else if (!"stt".equals(f.getKey()) && !"sources".equals(f.getKey())) {
          Double v = totals != null ? totals.get(f.getKey()) : null;
          if ("res_ontime".equals(f.getKey()) && !weekly && totals != null) {
            double a = totals.getOrDefault("res_ontime", 0.0);
            double b = totals.getOrDefault("res_before", 0.0);
            v = a + b;
          }
          if (v != null && v != 0) {
            cell.setCellValue(v);
            cell.setCellStyle(numStyle);
          }
        }
      }

      sheet.setColumnWidth(0, 6 * 256);
      sheet.setColumnWidth(1, 42 * 256);
      for (int c = 2; c < canon.size(); c++) {
        sheet.setColumnWidth(c, ("sources".equals(canon.get(c).getKey()) ? 26 : 13) * 256);
      }

      wb.write(bos);
      return bos.toByteArray();
    }
  }

  private Double fieldVal(UnitRow r, String key, boolean weekly) {
    if ("res_ontime".equals(key) && !weekly) {
      double a = r.get("res_ontime") != null ? r.get("res_ontime") : 0;
      double b = r.get("res_before") != null ? r.get("res_before") : 0;
      double s = a + b;
      return s != 0 ? s : null;
    }
    return r.get(key);
  }
}
