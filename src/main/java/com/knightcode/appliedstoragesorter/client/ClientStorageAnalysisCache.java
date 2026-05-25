package com.knightcode.appliedstoragesorter.client;

import com.knightcode.appliedstoragesorter.network.SorterAnalysisPayloadHandler;

/**
 * 客户端侧便捷访问入口，委托给 SorterAnalysisPayloadHandler 的缓存。
 * 游戏内 Screen 可通过此入口获取最新分析报告。
 */
public final class ClientStorageAnalysisCache {
    private ClientStorageAnalysisCache() {
    }

    /**
     * 获取缓存的报告原始 JSON 字符串。
     */
    public static String getRawJson() {
        return SorterAnalysisPayloadHandler.getRawJson();
    }

    /**
     * 是否有缓存数据。
     */
    public static boolean hasData() {
        return SorterAnalysisPayloadHandler.hasData();
    }

    /**
     * 清空缓存。
     */
    public static void clear() {
        SorterAnalysisPayloadHandler.clear();
    }
}
