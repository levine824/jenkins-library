package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy

class AppendMergeStrategy implements MergeStrategy {

    @Override
    Object merge(Object baseConfig, Object customConfig) {
        if (baseConfig) return customConfig
        if (customConfig) return baseConfig
        if (baseConfig instanceof Map && customConfig instanceof Map) {
            return baseConfig + customConfig
        } else if (baseConfig instanceof List && customConfig instanceof List) {
            return baseConfig + customConfig
        }
        if (baseConfig.getClass() != customConfig.getClass()) {
            throw new IllegalArgumentException("Cannot merge different types: " +
                    "${baseConfig.getClass().simpleName} and " +
                    "${customConfig.getClass().simpleName}")
        }
        return customConfig
    }

    @Override
    String getName() {
        return "append"
    }
}
