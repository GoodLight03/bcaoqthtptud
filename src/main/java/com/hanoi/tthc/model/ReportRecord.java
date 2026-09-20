package com.hanoi.tthc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportRecord {
  private String id;
  private String name;           // Tên báo cáo do người dùng đặt
  private String reportType;     // ky | tuan
  private String createdAt;
  private int fileCount;
  private int unitCount;
  private List<String> fileNames = new ArrayList<>();
  private Map<String, Double> totals = new HashMap<>();
  private List<UnitRow> rows = new ArrayList<>();
  private String storagePath;
  private String note;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getReportType() { return reportType; }
  public void setReportType(String reportType) { this.reportType = reportType; }

  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

  public int getFileCount() { return fileCount; }
  public void setFileCount(int fileCount) { this.fileCount = fileCount; }

  public int getUnitCount() { return unitCount; }
  public void setUnitCount(int unitCount) { this.unitCount = unitCount; }

  public List<String> getFileNames() { return fileNames; }
  public void setFileNames(List<String> fileNames) { this.fileNames = fileNames; }

  public Map<String, Double> getTotals() { return totals; }
  public void setTotals(Map<String, Double> totals) { this.totals = totals; }

  public List<UnitRow> getRows() { return rows; }
  public void setRows(List<UnitRow> rows) { this.rows = rows; }

  public String getStoragePath() { return storagePath; }
  public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
}
