package com.levine824.config.merger.strategy

class UniqueMergeStrategy extends AbstractMergeStrategy {
    @Override
    Map mergeMaps(Map baseConfig, Map customConfig) {
        baseConfig + customConfig
    }

    @Override
    List mergeLists(List baseConfig, List customConfig) {
        (baseConfig + customConfig).unique()
    }
}
