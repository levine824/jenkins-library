package com.levine824.jenkins.config

class ConfigMerger {

    static Map merge(Map baseConfig, Map customConfig) {
        return merge(baseConfig, customConfig, new MergeOptions())
    }

    static Map merge(Map baseConfig, Map customConfig, MergeOptions options) {
        return doMerge(baseConfig, customConfig, null, options)
    }

    private static Object doMerge(Object base, Object custom, MergeStrategy parentStrategy, MergeOptions options) {
        if (base instanceof Map && custom instanceof Map) {
            return doMerge((Map) base, (Map) custom, parentStrategy, options)
        } else if (base instanceof List && custom instanceof List) {
            return doMerge((List) base, (List) custom, parentStrategy, options)
        } else {
            if (base != null && custom != null && base.getClass() != custom.getClass()) {
                throw new IllegalArgumentException("Cannot merge different types: " +
                        "${base.getClass().simpleName} and ${custom.getClass().simpleName}")
            }
            return custom != null ? custom : base
        }
    }

    private static Map doMerge(Map baseMap, Map customMap, MergeStrategy parentStrategy, MergeOptions options) {
        Map mergedMap = new HashMap(baseMap)
        customMap.each { key, value ->
            mergedMap[key] = mergedMap.containsKey(key)
                    ? doMerge(mergedMap[key], value, parentStrategy, options)
                    : value
        }
        return mergedMap
    }

    private static List doMerge(List baseList, List customList, MergeStrategy parentStrategy, MergeOptions options) {
        def (strategy, cleanList) = resolveStrategy(customList, options.strategyKey)
        strategy = strategy ?: parentStrategy ?: options.defaultStrategy
        switch (strategy) {
            case MergeStrategy.REPLACE:
                return cleanList
            case MergeStrategy.APPEND:
                return appendMerge(baseList, cleanList)
            case MergeStrategy.UNIQUE:
                return uniqueMerge(baseList, cleanList)
            case MergeStrategy.DEEP_MERGE:
                return deepMerge(baseList, cleanList, strategy, options)
            case MergeStrategy.KEY_BASED:
                return keyBasedMerge(baseList, cleanList, strategy, options)
            default:
                throw new UnsupportedOperationException("Unhandled strategy: $strategy")
        }
    }

    private static Tuple2<MergeStrategy, List> resolveStrategy(List list, String key) {
        MergeStrategy strategy = null
        List cleanList = []
        list.each { element ->
            if (element instanceof Map && element.containsKey(key)) {
                if (strategy != null) {
                    throw new IllegalArgumentException("Multiple ${key} strategies defined in list")
                }
                strategy = MergeStrategy.from(element[key].toString())
            } else {
                cleanList << element
            }
        }
        return new Tuple2(strategy, cleanList)
    }

    private static List appendMerge(List baseList, List customList) {
        return baseList + customList
    }

    private static List uniqueMerge(List baseList, List customList) {
        return (baseList + customList).unique()
    }

    private static List deepMerge(List baseList, List customList, MergeStrategy strategy, MergeOptions options) {
        return keyBasedMerge(baseList, customList, strategy, options)
    }

    private static List keyBasedMerge(List baseList, List customList, MergeStrategy strategy, MergeOptions options) {
        def mergedList = new ArrayList(baseList)
        customList.each { customElement ->
            def baseElement = findElement(mergedList, customElement, options.uniqueKeys)
            if (baseElement) {
                def index = mergedList.indexOf(baseElement)
                mergedList[index] = (strategy == MergeStrategy.DEEP_MERGE)
                        ? doMerge(baseElement, customElement, strategy, options)
                        : customElement
            } else {
                mergedList << customElement
            }
        }
        return mergedList
    }

    private static Object findElement(List baseList, Object customElement, List<String> keys) {
        def key = findKey(customElement, keys)
        if (!key) return null
        return baseList.find { findKey(it, [key]) != null && customElement[key] == it }
    }

    private static String findKey(Object element, List<String> keys) {
        return (element instanceof Map)
                ? keys.find { element.containsKey(it) }
                : null
    }
}