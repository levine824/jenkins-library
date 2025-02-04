package com.levine824.jenkins.config

/**
 * Defines available merging strategies for configuration elements.
 * Strategies control how lists and maps are combined during merge operations.
 */
enum MergeStrategy {
    REPLACE,   // Completely replaces base elements with custom elements
    APPEND,    // Adds custom elements to the end of base elements
    UNIQUE,    // Combines elements and removes duplicates (order preserved)
    DEEP_MERGE,// Recursively merges nested structures
    KEY_BASED  // Merges elements based on unique identifier keys

    /**
     * Converts string representation to MergeStrategy enum.
     * @param strategy Case-insensitive strategy name (e.g., "deep_merge")
     * @return Corresponding MergeStrategy instance
     * @throws IllegalArgumentException for invalid strategy names
     */
    static MergeStrategy from(String strategy) {
        MergeStrategy mergeStrategy = values().find { it.name().equalsIgnoreCase(strategy) }
        if (!mergeStrategy) {
            throw new IllegalArgumentException("Invalid merge strategy: ${strategy}")
        }
        return mergeStrategy
    }
}