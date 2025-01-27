package com.levine824.jenkins.config

class ConfigMerger {
    // 合并策略枚举
    enum MergeStrategy {
        REPLACE,    // 完全覆盖
        APPEND,     // 追加元素
        UNIQUE,     // 去重追加
        DEEP_MERGE, // 深度合并
        KEY_BASED   // 基于唯一键合并
    }

    /**
     * 主入口：合并两个 Map 结构
     * @param baseMap 基础配置
     * @param overrideMap 覆盖配置
     * @return 合并后的配置
     */
    static Map<String, Object> mergeMaps(Map baseMap, Map overrideMap) {
        if (!baseMap) return overrideMap ?: [:] as Map<String, Object>
        if (!overrideMap) return baseMap ?: [:] as Map<String, Object>
        Map<String, Object> merged = new HashMap<>(baseMap)
        overrideMap.each { key, overrideValue ->
            Object baseValue = merged[key]
            // 处理策略标记（示例：key+ 表示追加）
            def (cleanKey, strategy) = parseStrategyMark(key)
            if (baseValue != null) {
                merged[cleanKey] = resolveMerge(baseValue, overrideValue, strategy)
            } else {
                merged[cleanKey] = overrideValue
            }
        }

        merged
    }

    /**
     * 解析键名中的策略标记
     * @param rawKey 原始键名
     * @return [清理后的键名, 合并策略]
     */
    private static Tuple2<String, MergeStrategy> parseStrategyMark(String rawKey) {
        switch (rawKey) {
            case ~/(.+)\+$/: // 追加策略
                return new Tuple2(rawKey[0..-2], MergeStrategy.APPEND)
            case ~/(.+)\^$/: // 深度合并
                return new Tuple2(rawKey[0..-2], MergeStrategy.DEEP_MERGE)
            case ~/(.+)\!$/: // 强制覆盖
                return new Tuple2(rawKey[0..-1], MergeStrategy.REPLACE)
            default:
                return new Tuple2(rawKey, MergeStrategy.DEEP_MERGE) // 默认深度合并
        }
    }

    /**
     * 根据数据类型选择合并方式
     */
    private static Object resolveMerge(Object base, Object override, MergeStrategy strategy) {
        if (base instanceof Map && override instanceof Map) {
            return mergeMaps((Map) base, (Map) override)
        } else if (base instanceof List && override instanceof List) {
            return mergeLists((List) base, (List) override, strategy)
        } else {
            // 基础类型直接使用覆盖值
            return override
        }
    }

    /**
     * 合并列表的核⼼逻辑
     */
    private static List<Object> mergeLists(List baseList, List overrideList, MergeStrategy strategy) {
        switch (strategy) {
            case MergeStrategy.REPLACE:
                return new ArrayList<>(overrideList)

            case MergeStrategy.APPEND:
                return baseList + overrideList

            case MergeStrategy.UNIQUE:
                return (baseList + overrideList).unique()

            case MergeStrategy.DEEP_MERGE:
                return deepMergeLists(baseList, overrideList)

            case MergeStrategy.KEY_BASED:
                return keyBasedMerge(baseList, overrideList)

            default:
                throw new IllegalArgumentException("Unsupported merge strategy: $strategy")
        }
    }

    /**
     * 深度合并列表（递归处理嵌套结构）
     */
    private static List<Object> deepMergeLists(List baseList, List overrideList) {
        List<Object> merged = new ArrayList<>(baseList)

        overrideList.each { overrideItem ->
            def matched = merged.find { baseItem ->
                if (baseItem instanceof Map && overrideItem instanceof Map) {
                    // 简单键匹配逻辑（可根据需求扩展）
                    baseItem.keySet() == ((Map) overrideItem).keySet()
                } else {
                    baseItem == overrideItem
                }
            }

            if (matched != null) {
                merged[merged.indexOf(matched)] = resolveMerge(matched, overrideItem, MergeStrategy.DEEP_MERGE)
            } else {
                merged.add(overrideItem)
            }
        }

        merged
    }

    /**
     * 基于唯一键合并（需元素为 Map 且包含 id 字段）
     */
    private static List<Object> keyBasedMerge(List baseList, List overrideList) {
        Map<Object, Object> mergedMap = [:]

        // 先合并基础列表
        baseList.each { item ->
            if (item instanceof Map && item.containsKey('id')) {
                mergedMap[item.id] = item
            } else {
                mergedMap.put(UUID.randomUUID(), item) // 无 id 项无法合并
            }
        }

        // 合并覆盖列表
        overrideList.each { overrideItem ->
            if (overrideItem instanceof Map && overrideItem.containsKey('id')) {
                def existing = mergedMap[overrideItem.id]
                mergedMap[overrideItem.id] = existing ?
                        mergeMaps(existing as Map, overrideItem as Map) :
                        overrideItem
            } else {
                mergedMap.put(UUID.randomUUID(), overrideItem)
            }
        }

        new ArrayList(mergedMap.values())
    }
}
