package cn.zhuatech.shiftmonitor;

import java.util.*;

/**
 * 沿用已保存的平滑分布计算总变差距离和以 2 为底的 Jensen–Shannon 散度（不是其平方根距离）。二者均在 0 至 1；逐类别展示占比变化的百分点及散度贡献。
 * 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Diagnostics {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static Map<String, Object> map(Object... pairs) {
    var m = new LinkedHashMap<String, Object>();
    for (int i = 0; i < pairs.length; i += 2) m.put((String) pairs[i], pairs[i + 1]);
    return m;
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static double num(Map<String, Object> m, String k) {
    return ((Number) m.get(k)).doubleValue();
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private static Double ratio(double a, double b) {
    return b == 0 ? null : a / b;
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @SuppressWarnings("unchecked")
  static Map<String, Object> evaluate(
      Map<String, Object> cfg, List<Map<String, Object>> rows, Map<String, Object> base) {
    var metrics = (Map<String, Object>) base.get("metrics");
    var evidence = (List<Map<String, Object>>) base.get("rows");
    var details = new ArrayList<Map<String, Object>>();
    double tv = 0, js = 0;
    for (var r : evidence) {
      double p = num(r, "before"), q = num(r, "after"), mid = (p + q) / 2;
      double part =
          Math.max(0, (p * Math.log(p / mid) + q * Math.log(q / mid)) / (2 * Math.log(2)));
      js += part;
      tv += Math.abs(q - p) / 2;
      details.add(map("id", r.get("id"), "shareChangePoints", 100 * (q - p), "jsPart", part));
    }
    return Checks.result(
        map("totalVariation", Math.min(1, tv), "jsDivergence", Math.min(1, js)), details);
  }
}
