package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy

class UniqueMergeStrategy implements MergeStrategy {

    @Override
    Object merge(Object baseConfig, Object customConfig) {
        Object mergedConfig = new AppendMergeStrategy().merge(baseConfig, customConfig)
        return mergedConfig instanceof List ? mergedConfig.unique() : mergedConfig
    }

    @Override
    String getName() {
        return "unique"
    }
}
