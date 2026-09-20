package com.hanoi.tthc.service;

import com.hanoi.tthc.model.CanonField;
import com.hanoi.tthc.model.UnitRow;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.*;
import java.util.Locale;

@Service
public class AggregateService {

  private final UnitNormalizeService unitNormalize;

  public AggregateService(UnitNormalizeService unitNormalize) {
    this.unitNormalize = unitNormalize;
  }

  public List<UnitRow> aggregate(List<UnitRow> allRows) {
    Map<String, UnitRow> map = new LinkedHashMap<>();
    List<String> numKeys = CanonField.numericKeys();

    for (UnitRow r : allRows) {
      if (r == null || r.getUnit() == null) continue;
      String key = unitNormalize.unitKey(r.getUnit());
      if (key == null || key.isBlank()) continue;
      String lower = key.toLowerCase();
      if (lower.contains("cap xa") || lower.contains("account khong dung")) continue;

      boolean hasNum = numKeys.stream().anyMatch(k -> r.get(k) != null);
      if (!hasNum) continue;

      if (!map.containsKey(key)) {
        UnitRow base = new UnitRow(r.getUnit());
        base.setStt(r.getStt());
        for (String k : numKeys) {
          Double v = r.get(k);
          base.put(k, v != null ? v : 0.0);
        }
        base.getSources().addAll(r.getSources());
        map.put(key, base);
      } else {
        UnitRow cur = map.get(key);
        for (String k : numKeys) {
          Double v = r.get(k);
          if (v != null) cur.add(k, v);
        }
        cur.getSources().addAll(r.getSources());
        if (r.getUnit().length() > cur.getUnit().length()) {
          cur.setUnit(r.getUnit());
        }
      }
    }

    List<UnitRow> arr = new ArrayList<>(map.values());
    Collator vi = Collator.getInstance(Locale.forLanguageTag("vi"));
    arr.sort(Comparator.comparing(UnitRow::getUnit, Comparator.nullsLast(vi)));
    for (int i = 0; i < arr.size(); i++) {
      arr.get(i).setStt(String.valueOf(i + 1));
    }
    return arr;
  }

  public Map<String, Double> totals(List<UnitRow> rows) {
    Map<String, Double> t = new LinkedHashMap<>();
    for (String k : CanonField.numericKeys()) {
      double s = 0;
      for (UnitRow r : rows) {
        Double v = r.get(k);
        if (v != null) s += v;
      }
      t.put(k, s);
    }
    return t;
  }
}
