package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy

class UniqueMergeStrategy implements MergeStrategy {

    @Override
    Object merge(Object base, Object custom) {
        Object mergedConfig = new AppendMergeStrategy().merge(base, custom)
        return mergedConfig instanceof List ? mergedConfig.unique() : mergedConfig
    }

    @Override
    String getName() {
        return "unique"
    }
}
