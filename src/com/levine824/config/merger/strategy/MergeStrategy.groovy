package com.levine824.config.merger.strategy

interface MergeStrategy {
    Object merge(Object baseConfig, Object customConfig)
}