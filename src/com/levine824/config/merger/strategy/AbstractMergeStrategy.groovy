package com.levine824.config.merger.strategy

abstract class AbstractMergeStrategy implements MergeStrategy {
    @Override
    Object merge(Object baseConfig, Object customConfig) {
        if (baseConfig == null) return customConfig
        if (customConfig == null) return baseConfig
        if (baseConfig instanceof Map && customConfig instanceof Map) {
            return mergeMaps(baseConfig, customConfig)
        } else if (baseConfig instanceof List && customConfig instanceof List) {
            return mergeLists(baseConfig, customConfig)
        }
        if (baseConfig.getClass() != customConfig.getClass()) {
            throw new IllegalArgumentException("Type mismatch: " +
                    "baseConfig is ${baseConfig.getClass()}, " +
                    "customConfig is ${customConfig.getClass()}"
            )
        }
        return customConfig
    }

    abstract Map mergeMaps(Map baseConfig, Map customConfig)

    abstract List mergeLists(List baseConfig, List customConfig)
}
