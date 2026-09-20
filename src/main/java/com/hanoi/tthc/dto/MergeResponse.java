package com.hanoi.tthc.dto;

import com.hanoi.tthc.model.UnitRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeResponse {
  private List<UnitRow> rows = new ArrayList<>();
  private Map<String, Double> totals = new HashMap<>();
  private int unitCount;
  private String savedId;

  public List<UnitRow> getRows() { return rows; }
  public void setRows(List<UnitRow> rows) { this.rows = rows; }

  public Map<String, Double> getTotals() { return totals; }
  public void setTotals(Map<String, Double> totals) { this.totals = totals; }

  public int getUnitCount() { return unitCount; }
  public void setUnitCount(int unitCount) { this.unitCount = unitCount; }

  public String getSavedId() { return savedId; }
  public void setSavedId(String savedId) { this.savedId = savedId; }
}
