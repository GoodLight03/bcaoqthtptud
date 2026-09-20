package com.hanoi.tthc.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;

@Service
public class UnitNormalizeService {

  public String normalizeUnitName(String name) {
    if (name == null) return "";
    return name.trim()
        .replaceAll("^[-–—\\s]+", "")
        .replaceAll("\\s+", " ")
        .trim();
  }

  public String removeDiacritics(String str) {
    if (str == null) return "";
    String n = Normalizer.normalize(str, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "");
    return n.replace('đ', 'd').replace('Đ', 'd');
  }

  /**
   * Canonical key để gộp đơn vị – port từ bản HTML chuẩn.
   */
  public String unitKey(String name) {
    String s = normalizeUnitName(name).toLowerCase();
    s = removeDiacritics(s);
    String original = s;

    s = s
        .replaceAll("ubnd\\s*", "")
        .replaceAll("uy\\s*ban\\s*nhan\\s*dan\\s*", "")
        .replaceAll(",?\\s*thanh\\s*pho\\s*ha\\s*noi", "")
        .replaceAll(",?\\s*tp\\.?\\s*ha\\s*noi", "")
        .replaceAll(",?\\s*ha\\s*noi", "")
        .replaceAll("\\s*-\\s*tp\\.?\\s*ha\\s*noi", "")
        .replaceAll("\\s*-\\s*thanh\\s*pho\\s*ha\\s*noi", "");

    s = s
        .replaceAll("ban\\s*qlckcnc\\s*-?\\s*kcn", "ban quan ly khu cnc va khu cn")
        .replaceAll("bql\\s*khu\\s*cnc\\s*va\\s*khu\\s*cn", "ban quan ly khu cnc va khu cn")
        .replaceAll("ban\\s*quan\\s*ly\\s*cac\\s*khu\\s*cong\\s*nghe\\s*cao\\s*va\\s*khu\\s*cong\\s*nghiep", "ban quan ly khu cnc va khu cn")
        .replaceAll("ban\\s*quan\\s*ly\\s*khu\\s*kinh\\s*te\\s*va\\s*khu\\s*cong\\s*nghiep", "ban quan ly khu cnc va khu cn");

    s = s.replaceAll("chi\\s*nhanh\\s*so\\s*(\\d+).*", "chi nhanh so $1");

    s = s
        .replaceAll("trung\\s*tam\\s*hcc\\b", "trung tam phuc vu hanh chinh cong")
        .replaceAll("trung\\s*tam\\s*phuc\\s*vu\\s*hanh\\s*chinh\\s*cong", "trung tam phuc vu hanh chinh cong");

    s = s
        .replaceAll("so\\s*van\\s*hoa\\s*,?\\s*the\\s*thao\\s*va\\s*du\\s*lich", "so van hoa the thao va du lich")
        .replaceAll("so\\s*van\\s*hoa\\s*va\\s*the\\s*thao", "so van hoa the thao va du lich")
        .replaceAll("so\\s*du\\s*lich", "so van hoa the thao va du lich")
        .replaceAll("so\\s*noi\\s*vu", "so noi vu")
        .replaceAll("so\\s*tai\\s*chinh", "so tai chinh")
        .replaceAll("so\\s*y\\s*te", "so y te")
        .replaceAll("so\\s*xay\\s*dung", "so xay dung")
        .replaceAll("so\\s*tu\\s*phap", "so tu phap")
        .replaceAll("so\\s*nong\\s*nghiep\\s*va\\s*moi\\s*truong", "so nong nghiep va moi truong")
        .replaceAll("so\\s*quy\\s*hoach\\s*-?\\s*kien\\s*truc", "so quy hoach kien truc")
        .replaceAll("so\\s*cong\\s*thuong", "so cong thuong")
        .replaceAll("so\\s*giao\\s*duc\\s*va\\s*dao\\s*tao", "so giao duc va dao tao")
        .replaceAll("so\\s*dan\\s*toc\\s*va\\s*ton\\s*giao", "so dan toc va ton giao")
        .replaceAll("so\\s*ngoai\\s*vu", "so ngoai vu");

    s = s
        .replaceAll("van\\s*phong\\s*ubnd\\s*thanh\\s*pho", "van phong ubnd thanh pho")
        .replaceAll("van\\s*phong\\s*ubnd\\s*tp\\.?", "van phong ubnd thanh pho");

    s = s
        .replaceAll("\\bphuong\\b", "phuong")
        .replaceAll("\\bxa\\b", "xa")
        .replaceAll("\\bquan\\b", "quan")
        .replaceAll("\\bhuyen\\b", "huyen");

    if (s.matches(".*\\bphuong\\b.*") || s.matches(".*\\bxa\\b.*")) {
      s = "ubnd " + s.replaceFirst("^ubnd\\s*", "");
    }

    s = s.replaceAll("[.,;:()–—\\-]", " ").replaceAll("\\s+", " ").trim();

    if (s.isEmpty()) {
      if (original.contains("van phong")) return "van phong ubnd thanh pho";
      return "ubnd thanh pho";
    }
    return s;
  }
}
