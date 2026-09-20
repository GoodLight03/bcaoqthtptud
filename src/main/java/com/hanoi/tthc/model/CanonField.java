package com.hanoi.tthc.model;

import java.util.ArrayList;
import java.util.List;

public class CanonField {
  private final String key;
  private final String top;
  private final String sub;

  public CanonField(String key, String top, String sub) {
    this.key = key;
    this.top = top;
    this.sub = sub;
  }

  public String getKey() { return key; }
  public String getTop() { return top; }
  public String getSub() { return sub; }

  /** Báo cáo Theo kỳ (đầy đủ) */
  public static List<CanonField> full() {
    List<CanonField> list = new ArrayList<>();
    list.add(new CanonField("stt", "STT", ""));
    list.add(new CanonField("unit", "Đơn vị giải quyết TTHC", ""));
    list.add(new CanonField("recv_total", "Số hồ sơ tiếp nhận", "Tổng số"));
    list.add(new CanonField("recv_direct", "Số hồ sơ tiếp nhận", "Tiếp nhận trực tiếp"));
    list.add(new CanonField("recv_online_full", "Số hồ sơ tiếp nhận", "Tiếp nhận trực tuyến toàn trình"));
    list.add(new CanonField("recv_online_partial", "Số hồ sơ tiếp nhận", "Tiếp nhận trực tuyến một phần"));
    list.add(new CanonField("recv_online_other", "Số hồ sơ tiếp nhận", "Tiếp nhận trực tuyến hình thức khác"));
    list.add(new CanonField("recv_non_local", "Số hồ sơ tiếp nhận", "Tiếp nhận phi địa giới"));
    list.add(new CanonField("recv_carry", "Số hồ sơ tiếp nhận", "Kỳ trước chuyển qua"));
    list.add(new CanonField("recv_postal", "Số hồ sơ tiếp nhận", "Bưu chính / BCCI"));
    list.add(new CanonField("res_total", "Số hồ sơ đã giải quyết", "Tổng số"));
    list.add(new CanonField("res_ontime", "Số hồ sơ đã giải quyết", "Đúng và trước hạn"));
    list.add(new CanonField("res_late_sorry", "Số hồ sơ đã giải quyết", "Quá hạn (đã có thư xin lỗi)"));
    list.add(new CanonField("res_late_no_sorry", "Số hồ sơ đã giải quyết", "Quá hạn (chưa có thư xin lỗi)"));
    list.add(new CanonField("res_late", "Số hồ sơ đã giải quyết", "Quá hạn (gộp)"));
    list.add(new CanonField("proc_total", "Số hồ sơ đang giải quyết", "Tổng số"));
    list.add(new CanonField("proc_ontime", "Số hồ sơ đang giải quyết", "Chưa đến hạn"));
    list.add(new CanonField("proc_late_sorry", "Số hồ sơ đang giải quyết", "Quá hạn (đã có thư xin lỗi)"));
    list.add(new CanonField("proc_late_no_sorry", "Số hồ sơ đang giải quyết", "Quá hạn (chưa có thư xin lỗi)"));
    list.add(new CanonField("proc_late", "Số hồ sơ đang giải quyết", "Quá hạn (gộp)"));
    list.add(new CanonField("pay_total", "Thanh toán hồ sơ", "Tổng số"));
    list.add(new CanonField("pay_online", "Thanh toán hồ sơ", "Trực tuyến"));
    list.add(new CanonField("pay_direct", "Thanh toán hồ sơ", "Trực tiếp"));
    list.add(new CanonField("stopped", "Hồ sơ dừng xử lý", ""));
    list.add(new CanonField("pending_ontime", "Hồ sơ TT chờ tiếp nhận", "Đúng hạn"));
    list.add(new CanonField("pending_late", "Hồ sơ TT chờ tiếp nhận", "Quá hạn"));
    list.add(new CanonField("pending_receive", "Hồ sơ TT chờ tiếp nhận", "Tổng (nếu không tách)"));
    list.add(new CanonField("rejected", "Hồ sơ không được tiếp nhận", ""));
    list.add(new CanonField("mismatch_digital", "HS kết thúc nhưng KQ điện tử ≠ giấy", ""));
    list.add(new CanonField("receipt_money", "HS thu tiền có xuất biên lai", ""));
    list.add(new CanonField("dn_verified_rate", "Tỷ lệ DN xác thực QLVBDN", ""));
    list.add(new CanonField("withdrawn", "Hồ sơ rút", ""));
    list.add(new CanonField("sources", "Nguồn file", ""));
    return list;
  }

  /** Báo cáo Theo Tuần */
  public static List<CanonField> weekly() {
    List<CanonField> list = new ArrayList<>();
    list.add(new CanonField("stt", "STT", ""));
    list.add(new CanonField("unit", "Đơn vị", ""));
    list.add(new CanonField("recv_total", "Số lượng hồ sơ tiếp nhận", "Tổng số"));
    list.add(new CanonField("recv_online_full", "Số lượng hồ sơ tiếp nhận", "Trực tuyến toàn trình"));
    list.add(new CanonField("recv_online_partial", "Số lượng hồ sơ tiếp nhận", "Trực tuyến một phần"));
    list.add(new CanonField("recv_postal", "Số lượng hồ sơ tiếp nhận", "Qua dịch vụ bưu chính"));
    list.add(new CanonField("recv_direct", "Số lượng hồ sơ tiếp nhận", "Trực tiếp"));
    list.add(new CanonField("recv_carry", "Số lượng hồ sơ tiếp nhận", "Từ kỳ trước"));
    list.add(new CanonField("res_total", "Số lượng hồ sơ đã giải quyết", "Tổng số"));
    list.add(new CanonField("res_before", "Số lượng hồ sơ đã giải quyết", "Trước hạn"));
    list.add(new CanonField("res_ontime", "Số lượng hồ sơ đã giải quyết", "Đúng hạn"));
    list.add(new CanonField("res_late", "Số lượng hồ sơ đã giải quyết", "Quá hạn"));
    list.add(new CanonField("proc_total", "Số lượng hồ sơ đang giải quyết", "Tổng số"));
    list.add(new CanonField("proc_ontime", "Số lượng hồ sơ đang giải quyết", "Trong hạn"));
    list.add(new CanonField("proc_late", "Số lượng hồ sơ đang giải quyết", "Quá hạn"));
    list.add(new CanonField("sources", "Nguồn file", ""));
    return list;
  }

  public static List<String> numericKeys() {
    List<String> keys = new ArrayList<>();
    for (CanonField c : full()) {
      if (!"stt".equals(c.key) && !"unit".equals(c.key) && !"sources".equals(c.key)) {
        keys.add(c.key);
      }
    }
    if (!keys.contains("res_before")) keys.add("res_before");
    return keys;
  }
}
