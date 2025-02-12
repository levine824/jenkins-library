package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy

class UniqueMergeStrategy implements MergeStrategy {

    @Override
    Object merge(Object baseConfig, Object customConfig) {
        Object mergedConfig = new AppendMergeStrategy().merge(baseConfig, customConfig)
        // TODO: 目前只使用默认的数字去重，是否要考虑允许用户自定义去重函数输入
        return mergedConfig instanceof List ? mergedConfig.unique() : mergedConfig
    }

    @Override
    String getName() {
        return "unique"
    }
}
