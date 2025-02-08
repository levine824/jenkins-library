package com.levine824.jenkins.config

class MergeOptions {
    String uniqueKey = "@merge"
    String defaultStrategy = "deepMerge"
    Map<String, MergeStrategy> strategies = [:]

    register(MergeStrategy strategy) {
        strategies.put(strategy.getName(), strategy)
    }
}
