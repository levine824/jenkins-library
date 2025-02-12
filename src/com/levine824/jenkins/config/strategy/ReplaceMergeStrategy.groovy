package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy

class ReplaceMergeStrategy implements MergeStrategy {

    @Override
    Object merge(Object baseConfig, Object customConfig) {
        if (baseConfig instanceof Map && customConfig instanceof Map) {
            return baseConfig + customConfig
        } else {
            return customConfig
        }
    }

    @Override
    String getName() {
        return "replace"
    }
}
