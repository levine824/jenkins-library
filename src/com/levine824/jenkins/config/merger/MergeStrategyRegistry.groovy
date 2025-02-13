package com.levine824.jenkins.config.merger

import com.levine824.jenkins.config.strategy.AppendMergeStrategy
import com.levine824.jenkins.config.strategy.ReplaceMergeStrategy
import com.levine824.jenkins.config.strategy.UniqueMergeStrategy

class MergeStrategyRegistry {
    private static final Map<String, MergeStrategy> strategies = [:].asSynchronized()

    static {
        AppendMergeStrategy append = new AppendMergeStrategy()
        ReplaceMergeStrategy replace = new ReplaceMergeStrategy()
        UniqueMergeStrategy unique = new UniqueMergeStrategy()
        strategies.put(append.getName(), append)
        strategies.put(replace.getName(), replace)
        strategies.put(unique.getName(), unique)
    }

    static void register(MergeStrategy strategy) {
        if (!strategy) {
            throw new IllegalArgumentException("Strategy cannot be null")
        }
        String name = strategy.getName()
        if (strategies.containsKey(name)) {
            throw new IllegalStateException("Strategy '${name}' is already registered")
        }
        strategies[name] = strategy
    }

    static MergeStrategy getStrategy(String name) {
        return strategies.get(name)
    }

    static Set<String> getRegisteredStrategies() {
        return strategies.keySet().asImmutable()
    }

    static void clear() {
        strategies.clear()
    }
}
