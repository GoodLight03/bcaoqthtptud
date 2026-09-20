package com.hanoi.tthc.dto;

import com.hanoi.tthc.model.UnitRow;

import java.util.ArrayList;
import java.util.List;

public class MergeRequest {
  /** ky | tuan */
  private String reportType = "ky";
  private boolean save = false;
  /** Tên báo cáo khi lưu */
  private String name;
  private List<String> fileNames = new ArrayList<>();
  private List<UnitRow> rows = new ArrayList<>();

  public String getReportType() { return reportType; }
  public void setReportType(String reportType) { this.reportType = reportType; }

  public boolean isSave() { return save; }
  public void setSave(boolean save) { this.save = save; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public List<String> getFileNames() { return fileNames; }
  public void setFileNames(List<String> fileNames) { this.fileNames = fileNames; }

  public List<UnitRow> getRows() { return rows; }
  public void setRows(List<UnitRow> rows) { this.rows = rows; }
}
