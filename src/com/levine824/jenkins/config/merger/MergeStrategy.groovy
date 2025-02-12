package com.levine824.jenkins.config.merger

interface MergeStrategy {
    Object merge(Object baseConfig, Object customConfig)

    String getName()
}