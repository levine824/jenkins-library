package com.levine824.jenkins.config.merger

class ConfigMerger {
    private MergeStrategy strategy

    ConfigMerger(MergeStrategy strategy) {
        this.strategy = strategy
    }

    Map merge(Map baseConfig, Map... customConfigs) {
        Map mergedConfig = [:] + baseConfig
        customConfigs.each { customConfig ->
            strategy.merge(mergedConfig, customConfig)
        }
        return mergedConfig
    }
}
