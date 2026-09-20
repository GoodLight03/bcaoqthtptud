package com.hanoi.tthc.model;

import java.util.ArrayList;
import java.util.List;

public class ParsedFile {
  private String name;
  private String sheetName;
  private String familyCode;   // A, B, C, unknown
  private String familyLabel;
  private List<UnitRow> rows = new ArrayList<>();
  private String error;
  private int numericRowCount;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getSheetName() { return sheetName; }
  public void setSheetName(String sheetName) { this.sheetName = sheetName; }

  public String getFamilyCode() { return familyCode; }
  public void setFamilyCode(String familyCode) { this.familyCode = familyCode; }

  public String getFamilyLabel() { return familyLabel; }
  public void setFamilyLabel(String familyLabel) { this.familyLabel = familyLabel; }

  public List<UnitRow> getRows() { return rows; }
  public void setRows(List<UnitRow> rows) { this.rows = rows; }

  public String getError() { return error; }
  public void setError(String error) { this.error = error; }

  public int getNumericRowCount() { return numericRowCount; }
  public void setNumericRowCount(int numericRowCount) { this.numericRowCount = numericRowCount; }
}
