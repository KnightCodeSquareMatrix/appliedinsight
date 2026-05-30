# RuntimeMoveAnalysisReport

| 维度 | 说明 |
|------|------|
| **包路径** | `ae2/analysis/RuntimeMoveAnalysisReport.java` |
| **定位** | 搬运可行性分析结果（record） |
| **职责** | 承载 `Ae2MoveAnalyzer` 的分析结果：是否完全可接纳 + 阻塞原因列表 |
| **字段** | `fullyAdmissible` (`boolean`)、`blockingReasons` (`List<String>`) |
| **被谁使用** | `Ae2MoveAnalyzer` 的输出 |
