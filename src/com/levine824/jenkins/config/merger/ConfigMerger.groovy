package com.levine824.jenkins.config.merger

class ConfigMerger {
    private MergeStrategy strategy

    ConfigMerger(MergeStrategy strategy) {
        this.strategy = strategy
    }

    ConfigMerger(String strategyName) {
        this.strategy = MergeStrategyRegistry.getStrategy(strategyName)
        if (!this.strategy) {
            throw new IllegalArgumentException("The strategy does not exist: ${strategyName}")
        }
    }

    merge(Map baseConfig, Map... customConfigs) {
        Map mergedConfig = [:] + baseConfig
        customConfigs.each { customConfig ->
            strategy.merge(mergedConfig, customConfig)
        }
        return mergedConfig
    }
}
