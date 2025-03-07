package com.levine824.jenkins.config

class DefaultConfigMerger implements ConfigMerger {
    static final String STRATEGY_MARKER = '@merge'
    static final String STRATEGY_REPLACE = 'replace'
    static final String STRATEGY_APPEND = 'append'
    static final String STRATEGY_UNIQUE = 'unique'
    static final String STRATEGY_DEEP = 'deep'
    static final String STRATEGY_KEY_BASED = 'keyBased'

    String defaultStrategy = STRATEGY_DEEP
    List<String> uniqueKeys = ['id', 'name']

    Object merge(Object base, Object custom) {
        if (base == null) return custom
        if (custom == null) return base
        if (base instanceof Map && custom instanceof Map) {
            return mergeMap(base, custom)
        } else if (base instanceof List && custom instanceof List) {
            return mergeList(base, custom)
        }
        if (base.getClass() != custom.getClass()) {
            throw new IllegalArgumentException('Cannot merge different types: ' +
                    "${base.getClass().simpleName} and " +
                    "${custom.getClass().simpleName}")
        }
        return custom
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
            throw new IllegalArgumentException('Multiple strategy markers found in list')
        }
        String strategy = strategies
                ? strategies.last()[STRATEGY_MARKER]
                : defaultStrategy
        List cleanedCustom = custom + []
        cleanedCustom.removeAll(strategies)
        return mergeList(base, cleanedCustom, strategy)
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
        custom.each { customElement ->
            String uniqueKey = findUniqueKey(customElement)
            int index = -1
            if (uniqueKey) {
                index = base.findIndexOf {
                    it instanceof Map && customElement[uniqueKey] == it[uniqueKey]
                }
            }
            if (index >= 0) {
                merged[index] = handler.call(base[index], customElement)
            } else {
                merged << customElement
            }
        }
        return merged
    }

    private String findUniqueKey(Object o) {
        return o instanceof Map
                ? uniqueKeys.find { uniqueKey -> o.containsKey(uniqueKey) }
                : null
    }
}