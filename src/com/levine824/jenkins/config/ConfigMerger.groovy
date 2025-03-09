package com.levine824.jenkins.config

interface ConfigMerger {
    Map merge(Map base, Map custom)
}