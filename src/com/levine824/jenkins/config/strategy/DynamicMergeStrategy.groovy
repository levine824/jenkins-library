package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy

class DynamicMergeStrategy implements MergeStrategy {
    public static final String STRATEGY_KEY = "@merge"

    private Map<String, MergeStrategy> strategies = [:]

    private MergeStrategy defaultStrategy

    void register(MergeStrategy strategy) {
        if (!strategy) {
            throw new IllegalArgumentException("Strategy cannot be null")
        }
        strategies.put(strategy.getName(), strategy)
    }

    @Override
    Object merge(Object base, Object custom) {
        MergeStrategy strategy = parse(custom) ?: defaultStrategy
        return strategy.merge(base, custom)
    }

    MergeStrategy parse(Object obj) {
        String name = null
        if (obj instanceof Map) {
            name = obj[STRATEGY_KEY]
        } else if (obj instanceof List) {
            List names = obj.findAll {
                it instanceof Map && it.containsKey(STRATEGY_KEY)
            }
            if (names.size() > 1) {
                throw new Exception("Multiple strategy keys found in list")
            }
            name = names[0][STRATEGY_KEY]
        }
        return strategies[name]
    }

    @Override
    String getName() {
        return "dynamic"
    }
}
