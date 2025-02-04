package com.levine824.jenkins.config

/**
 * Configuration options for merging strategies, including strategy marker key,
 * default strategy, and unique identifier keys for elements.
 */
class MergeOptions {
    // Special key used in lists to specify merge strategy (e.g., @merge: 'APPEND')
    String strategyKey = '@merge'

    // Default merging strategy when no explicit strategy is specified
    MergeStrategy defaultStrategy = MergeStrategy.DEEP_MERGE

    // List of keys used to uniquely identify elements during KEY_BASED merging
    List<String> uniqueKeys = ['id', 'name']
}
