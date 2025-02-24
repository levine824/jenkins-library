package com.levine824.jenkins.config

class DefaultConfigMerger implements ConfigMerger {
    static final String STRATEGY_MARKER = "@merge"
    static final String STRATEGY_REPLACE = "replace"
    static final String STRATEGY_APPEND = "append"
    static final String STRATEGY_UNIQUE = "unique"
    static final String STRATEGY_DEEP = "deep"
    static final String STRATEGY_KEY_BASED = "keyBased"

    String defaultStrategy = STRATEGY_DEEP
    List<String> uniqueKeys = ["id", "name"]

    Object merge(Object baseConfig, Object customConfig) {
        if (baseConfig == null) return customConfig
        if (customConfig == null) return baseConfig
        if (baseConfig instanceof Map && customConfig instanceof Map) {
            return mergeMap(baseConfig, customConfig)
        } else if (baseConfig instanceof List && customConfig instanceof List) {
            return mergeList(baseConfig, customConfig)
        }
        if (baseConfig.getClass() != customConfig.getClass()) {
            throw new IllegalArgumentException("Cannot merge different types: " +
                    "${baseConfig.getClass().simpleName} and " +
                    "${customConfig.getClass().simpleName}")
        }
        return customConfig
    }

    private Map mergeMap(Map base, Map custom) {
        Map merged = base + [:]
        custom.each { key, value ->
            merged[key] = merged.containsKey(key)
                    ? merge(merged[key], value)
                    : value
        }
        return merged
    }

    private List mergeList(List base, List custom) {
        List strategies = custom.findAll {
            it instanceof Map && it.containsKey(STRATEGY_MARKER)
        }
        if (strategies.size() > 1) {
            throw new IllegalArgumentException("Multiple strategy markers found in list")
        }
        String strategy = strategies
                ? strategies.last()[STRATEGY_MARKER]
                : defaultStrategy
        List filtered = custom + []
        filtered.removeAll(strategies)
        return mergeList(base, filtered, strategy)
    }

    private List mergeList(List base, List custom, String strategy) {
        switch (strategy) {
            case STRATEGY_REPLACE:
                return custom
            case STRATEGY_APPEND:
                return base + custom
            case STRATEGY_UNIQUE:
                return (base + custom).unique()
            case STRATEGY_DEEP:
                return deepMerge(base, custom)
            case STRATEGY_KEY_BASED:
                return keyBasedMerge(base, custom)
            default:
                throw new IllegalArgumentException("Unhandled strategy: ${strategy}")
        }
    }

    private List deepMerge(List base, List custom) {
        return keyBasedMerge(base, custom) { be, ce -> merge(be, ce) }
    }

    private List keyBasedMerge(List base, List custom) {
        return keyBasedMerge(base, custom) { be, ce -> ce }
    }

    private List keyBasedMerge(List base, List custom, Closure handler) {
        List merged = base + []
        custom.each { ce ->
            String key = findKey(ce)
            int index = -1
            if (key) {
                index = base.findIndexOf {
                    it instanceof Map && ce[key] == it[key]
                }
            }
            if (index >= 0) {
                Object be = base[index]
                merged[index] = handler.call(be, ce)
            } else {
                merged << ce
            }
        }
        return merged
    }

    private String findKey(Object o) {
        return o instanceof Map
                ? uniqueKeys.find { o.containsKey(it) }
                : null
    }
}