package cn.zhuatech.shiftmonitor;

import static cn.zhuatech.shiftmonitor.Checks.*;

import java.util.*;

/**
 * 比较基准期与观察期的类别频数，计算平滑 PSI、各类别贡献与阈值告警，显式保留新出现或消失的类别。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Algorithm {
  /**
   * 需要同一分类口径下的全量计数；PSI 为描述性指标，不是显著性检验。 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  static Map<String, Object> compute(Map<String, Object> cfg, List<Map<String, Object>> rows) {
    var base = calculate(cfg, rows);
    var enriched = new LinkedHashMap<String, Object>(base);
    enriched.put("inspection", Inspection.evaluate(cfg, rows, base));
    enriched.put("diagnostics", Diagnostics.evaluate(cfg, rows, base));
    return enriched;
  }

  /**
   * 保留原始计算口径。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  private static Map<String, Object> calculate(
      Map<String, Object> cfg, List<Map<String, Object>> rows) {
    must(rows != null && !rows.isEmpty() && rows.size() <= 200, "需要 1–200 行");
    unique(rows);
    var evidence = new ArrayList<Map<String, Object>>();
    double alpha = num(cfg, "smoothing", 0.000001, 100),
        threshold = num(cfg, "alertAt", 0.000001, 100);
    long b = 0, c = 0;
    for (var r : rows) {
      int x = integer(r, "baselineCount", 0, 1000000000),
          y = integer(r, "currentCount", 0, 1000000000);
      must(x + y > 0, "两期不能同时为零");
      b += x;
      c += y;
    }
    must(b > 0 && c > 0, "基准期和观察期都需要样本");
    double psi = 0;
    for (var r : rows) {
      double
          p = (((Number) r.get("baselineCount")).longValue() + alpha) / (b + rows.size() * alpha),
          q = (((Number) r.get("currentCount")).longValue() + alpha) / (c + rows.size() * alpha);
      double d = (q - p) * Math.log(q / p);
      psi += d;
      evidence.add(
          Map.of(
              "id",
              r.get("id"),
              "before",
              p,
              "after",
              q,
              "contribution",
              d,
              "direction",
              q > p ? "上升" : q < p ? "下降" : "持平"));
    }
    return result(
        Map.of("psi", psi, "alert", psi >= threshold, "categories", rows.size()), evidence);
  }
}
