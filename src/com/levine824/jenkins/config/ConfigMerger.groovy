package com.levine824.jenkins.config

interface ConfigMerger {
    Object merge(Object base, Object custom)
}