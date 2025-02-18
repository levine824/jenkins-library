package com.levine824.jenkins.config.merger

interface MergeStrategy {
    Object merge(Object base, Object custom)

    String getName()
}