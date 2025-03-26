package com.levine824.config.merger.strategy

class ReplaceMergeStrategy extends AbstractMergeStrategy {
    @Override
    Map mergeMaps(Map baseConfig, Map customConfig) {
        customConfig + [:]
    }

    @Override
    List mergeLists(List baseConfig, List customConfig) {
        customConfig + []
    }
}
