package com.levine824.jenkins.config

enum MergeStrategy {
    REPLACE,
    APPEND,
    UNIQUE,
    DEEP_MERGE,
    KEY_BASED

    static MergeStrategy from(String strategy) {
        MergeStrategy mergeStrategy =
                values().find { it.name().equalsIgnoreCase(strategy) }
        if (!mergeStrategy) {
            throw new IllegalArgumentException("Invalid merge strategy: ${strategy}")
        }
        return mergeStrategy
    }
}