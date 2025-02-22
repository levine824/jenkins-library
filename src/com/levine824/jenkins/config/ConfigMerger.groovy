package com.levine824.jenkins.config

class ConfigMerger {
    public static final String STRATEGY_MARKER = "@merge"
    public static final String STRATEGY_REPLACE = "replace"
    public static final String STRATEGY_APPEND = "append"
    public static final String STRATEGY_UNIQUE = "unique"
    public static final String STRATEGY_DEEP = "deep"
    public static final String STRATEGY_KEY_BASED = "keyBased"

    String defaultStrategy = STRATEGY_DEEP
    List<String> uniqueKeys = ["id", "name"]

    Object merge(Object baseConfig, Object customConfig) {
        if (baseConfig == null) return customConfig
        if (customConfig == null) return baseConfig
        if (baseConfig instanceof Map && customConfig instanceof Map) {
            return mergeMap((Map) baseConfig, (Map) customConfig)
        } else if (baseConfig instanceof List && customConfig instanceof List) {
            return mergeList((List) baseConfig, (List) customConfig)
        }
        if (baseConfig.getClass() != customConfig.getClass()) {
            throw new IllegalArgumentException("Cannot merge different types: " +
                    "${baseConfig.getClass().simpleName} and " +
                    "${customConfig.getClass().simpleName}")
        }
        return customConfig
    }

    private Map mergeMap(Map baseMap, Map customMap) {
        Map mergedMap = baseMap + [:]
        customMap.each { key, value ->
            mergedMap[key] = mergedMap.containsKey(key)
                    ? merge(mergedMap[key], value)
                    : value
        }
        return mergedMap
    }

    private List mergeList(List baseList, List customList) {
        List strategies = customList.findAll {
            it instanceof Map && it.containsKey(STRATEGY_MARKER)
        }
        if (strategies.size() > 1) {
            throw new IllegalArgumentException("Multiple strategy markers found in list")
        }
        String strategy = strategies
                ? strategies.last()[STRATEGY_MARKER]
                : defaultStrategy
        List cleanList = customList + []
        cleanList.removeAll(strategies)
        return mergeList(baseList, cleanList, strategy)
    }

    private List mergeList(List baseList, List customList, String strategy) {
        switch (strategy) {
            case STRATEGY_REPLACE:
                return customList
            case STRATEGY_APPEND:
                return baseList + customList
            case STRATEGY_UNIQUE:
                return (baseList + customList).unique()
            case STRATEGY_DEEP:
                return deepMerge(baseList, customList)
            case STRATEGY_KEY_BASED:
                return keyBasedMerge(baseList, customList)
            default:
                throw new IllegalArgumentException("Unhandled strategy: $strategy")
        }
    }

    private List deepMerge(List baseList, List customList) {
        return keyBasedMerge(baseList, customList) { be, ce -> merge(be, ce) }
    }

    private List keyBasedMerge(List baseList, List customList) {
        return keyBasedMerge(baseList, customList) { be, ce -> ce }
    }

    private List keyBasedMerge(List baseList, List customList, Closure handler) {
        List mergedList = baseList + []
        customList.each { customElement ->
            String key = findKey(customElement)
            int index = -1
            if (key) {
                index = baseList.findIndexOf {
                    it instanceof Map && customElement[key] == it[key]
                }
            }
            if (index >= 0) {
                Object baseElement = baseList[index]
                mergedList[index] = handler.call(baseElement, customElement)
            } else {
                mergedList << customElement
            }
        }
        return mergedList
    }

    private String findKey(Object o) {
        return o instanceof Map
                ? uniqueKeys.find { o.containsKey(it) }
                : null
    }
}