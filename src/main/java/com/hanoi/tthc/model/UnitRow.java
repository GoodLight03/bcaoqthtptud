package com.hanoi.tthc.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class UnitRow {
  private String stt;
  private String unit;
  private Map<String, Double> numbers = new LinkedHashMap<>();
  private Set<String> sources = new LinkedHashSet<>();

  public UnitRow() {}

  public UnitRow(String unit) {
    this.unit = unit;
  }

  public String getStt() { return stt; }
  public void setStt(String stt) { this.stt = stt; }

  public String getUnit() { return unit; }
  public void setUnit(String unit) { this.unit = unit; }

  public Map<String, Double> getNumbers() { return numbers; }
  public void setNumbers(Map<String, Double> numbers) { this.numbers = numbers; }

  public Set<String> getSources() { return sources; }
  public void setSources(Set<String> sources) { this.sources = sources; }

  public Double get(String key) {
    return numbers.get(key);
  }

  public void put(String key, Double value) {
    if (value != null) {
      numbers.put(key, value);
    }
  }

  public void add(String key, double value) {
    numbers.merge(key, value, Double::sum);
  }

  public String sourcesJoined() {
    return String.join("; ", sources);
  }
}
