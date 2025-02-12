package com.levine824.jenkins.config.merger

class MergeStrategyRegistry {
    private static final Map<String, MergeStrategy> strategies = [:].asSynchronized()

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
