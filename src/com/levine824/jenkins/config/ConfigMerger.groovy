package com.levine824.jenkins.config

class ConfigMerger {

    enum MergeStrategy {
        REPLACE,    // 完全覆盖
        APPEND,     // 追加元素
        UNIQUE,     // 去重追加
        DEEP_MERGE, // 深度合并
        KEY_BASED   // 基于唯一键合并
    }

    static Map merge(Map base, Map override) {
        return mergeMaps(base, override)
    }

    private static Map mergeMaps(Map<String, Object> baseMap, Map<String, Object> overrideMap) {
        if (!baseMap) return overrideMap ?: [:]
        if (!overrideMap) return baseMap ?: [:]
        def mergedMap = new HashMap<String, Object>(baseMap)
        overrideMap.each { overrideKey, overrideValue ->
            def baseValue = mergedMap[overrideKey]
            // 处理策略标记（示例：key+ 表示追加）
            def (cleanKey, strategy) = parseStrategyMark(overrideKey)
            if (baseValue != null) {
                mergedMap[cleanKey] = mergeWithStrategy(baseValue, overrideValue, strategy)
            } else {
                mergedMap[cleanKey] = overrideValue
            }
        }
        return mergedMap
    }

    private static Tuple2<String, MergeStrategy> parseStrategyMark(String rawKey) {
        switch (rawKey) {
            case ~/(.+)\+$/: // 追加策略
                return new Tuple2(rawKey[0..-2], MergeStrategy.APPEND)
            case ~/(.+)\^$/: // 深度合并
                return new Tuple2(rawKey[0..-2], MergeStrategy.DEEP_MERGE)
            case ~/(.+)\!$/: // 强制覆盖
                return new Tuple2(rawKey[0..-1], MergeStrategy.REPLACE)
            default: // 默认深度合并
                return new Tuple2(rawKey, MergeStrategy.DEEP_MERGE)
        }
    }

    private static Object mergeWithStrategy(Object base, Object override, MergeStrategy strategy) {
        if (base instanceof Map && override instanceof Map) {
            return mergeMaps(base, override)
        } else if (base instanceof List && override instanceof List) {
            return mergeLists(base, override, strategy)
        } else {
            // 基础类型直接使用覆盖值
            return override
        }
    }

    private static List mergeLists(List baseList, List overrideList, MergeStrategy strategy) {
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
                return keyBasedMergeLists(baseList, overrideList, 'id')
            default:
                throw new IllegalArgumentException("Unsupported merge strategy: ${strategy}")
        }
    }

    private static List<Object> deepMergeLists(List baseList, List overrideList) {
        def (baseKeyInfo, baseNoKey) = buildKeyInfo(baseList)
        def (overrideKeyInfo, overrideNoKey) = buildKeyInfo(overrideList)
        def mergedList = []
        // 保留基础列表顺序并合并匹配项
        baseList.each { element ->
            def key = getElementKey(element)
            if (key != null) {
                def overrideElement = overrideKeyInfo.map.remove(key)
                mergedList.add(overrideElement ?
                        mergeWithStrategy(element, overrideElement, MergeStrategy.DEEP_MERGE)
                        : element)
            } else {
                mergedList.add(element)
            }
        }
        // 按原顺序添加覆盖列表剩余元素
        overrideKeyInfo.order.each { key ->
            if (overrideKeyInfo.map.containsKey(key)) mergedList.add(overrideKeyInfo.map[key])
        }
        // 追加无键元素
        mergedList.addAll(overrideNoKey)
        return mergedList
    }

    private static Tuple2<Map, List> buildKeyInfo(List list) {
        def keyMap = [:]
        def keyOrder = []
        def noKey = []
        list.each { element ->
            def key = getElementKey(element)
            if (key != null) {
                if (!keyMap.containsKey(key)) keyOrder.add(key)
                keyMap[key] = element
            } else {
                noKey.add(element)
            }
        }
        return new Tuple2([map: keyMap, order: keyOrder], noKey)
    }

    private static Object getElementKey(Object element) {
        if (element instanceof Map) {
            def foundKey = ['id', 'name', 'key'].find { element.containsKey(it) }
            return foundKey ? element[foundKey] : null
        }
    }

    private static List<Object> keyBasedMergeLists(
            List baseList, List overrideList, String uniqueKey = 'id') {
        def mergedMap = [:]
        baseList.each { element ->
            def key = element instanceof Map ? element[uniqueKey] : null
            if (key != null) mergedMap[key] = element
        }
        overrideList.each { element ->
            def key = element instanceof Map ? element[uniqueKey] : null
            if (key != null) {
                mergedMap[key] = mergedMap.containsKey(key) ?
                        mergeMaps((Map) mergedMap[key], (Map) element) : element
            }
        }
        return (baseList + overrideList).findAll { element ->
            element instanceof Map ? !mergedMap.containsKey(element[uniqueKey]) : true
        } + mergedMap.values()
    }

}
