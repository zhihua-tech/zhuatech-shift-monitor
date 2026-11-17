package cn.zhuatech.shiftmonitor;

import static cn.zhuatech.shiftmonitor.Checks.*;

import java.util.*;

/**
 * 按 PSI 贡献从高到低给出占比和累计占比，标出解释至少 80% 漂移所需的最小类别集合；零漂移时不生成整改对象。 上海如静知华信息科技有限公司 ·
 * https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Inspection {
  /**
   * 有序结果，可表达无法估计的数值。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  private static Map<String, Object> fields(Object... pairs) {
    var row = new LinkedHashMap<String, Object>();
    for (int i = 0; i < pairs.length; i += 2) row.put((String) pairs[i], pairs[i + 1]);
    return row;
  }

  /**
   * 对已校验的输入生成复核证据。上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ ·
   * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
   */
  @SuppressWarnings("unchecked")
  static Map<String, Object> evaluate(
      Map<String, Object> cfg, List<Map<String, Object>> rows, Map<String, Object> base) {
    var details = new ArrayList<Map<String, Object>>();
    var m = (Map<String, Object>) base.get("metrics");
    var output = (List<Map<String, Object>>) base.get("rows");
    var sorted = new ArrayList<>(output);
    sorted.sort(
        Comparator.<Map<String, Object>>comparingDouble(
                v -> ((Number) v.get("contribution")).doubleValue())
            .reversed()
            .thenComparing(v -> v.get("id").toString()));
    double sum = ((Number) m.get("psi")).doubleValue(), acc = 0;
    int priority = 0;
    for (var v : sorted) {
      double d = ((Number) v.get("contribution")).doubleValue();
      boolean take = sum > 0 && acc < 0.8 * sum;
      if (take) priority++;
      acc += d;
      details.add(
          fields(
              "id",
              v.get("id"),
              "contribution",
              d,
              "share",
              sum == 0 ? null : d / sum,
              "cumulative",
              sum == 0 ? null : Math.min(1, acc / sum),
              "priority",
              take));
    }
    return result(fields("priorityCategories", priority), details);
  }
}
