package com.levine824.jenkins.config

/**
 * Merges configuration maps with support for nested structures and list merge strategies.
 * Handles Map, List, and scalar values with various merge strategies.
 */
class ConfigMerger {

    /**
     * Enum defining all supported merge strategies for list operations
     */
    enum MergeStrategy {
        REPLACE,   // Completely replace source list
        APPEND,    // Append elements with index-based merging
        UNIQUE,    // Append elements and deduplicate
        DEEP_MERGE,// Recursive deep merge of elements
        KEY_BASED  // Merge based on unique identifier keys

        /**
         * Parse strategy from string value (case-insensitive)
         * @param strategy Strategy name to parse
         * @return Matching MergeStrategy instance
         * @throws IllegalArgumentException for unknown strategies
         */
        static MergeStrategy from(String strategy) {
            return values().find { it.name().equalsIgnoreCase(strategy) }
                    ?: null
        }
    }

    /**
     * Main entry point for merging two configuration maps
     * @param source Original configuration map
     * @param target Target configuration map with merge instructions
     * @return Merged configuration map
     */
    Map merge(Map source, Map target) {
        return mergeMaps(source, target)
    }

    /**
     * Recursively merge two maps
     * @param source Source map to merge into
     * @param target Target map containing merge operations
     * @return New merged map
     */
    private Map mergeMaps(Map source, Map target) {
        Map merged = new HashMap(source)
        target.each { key, targetValue ->
            merged[key] = merged.containsKey(key)
                    ? mergeValues(merged[key], targetValue, null)
                    : targetValue
        }
        return merged
    }

    /**
     * Merge two values based on their types and parent strategy
     * @param sourceValue Value from source configuration
     * @param targetValue Value from target configuration
     * @param parentStrategy Active merge strategy from parent context
     * @return Merged result
     */
    private Object mergeValues(Object sourceValue, Object targetValue, MergeStrategy parentStrategy) {
        if (sourceValue instanceof Map && targetValue instanceof Map) {
            return mergeMaps((Map) sourceValue, (Map) targetValue)
        } else if (sourceValue instanceof List && targetValue instanceof List) {
            return mergeLists((List) sourceValue, (List) targetValue, parentStrategy)
        } else {
            return targetValue  // Scalar replacement
        }
    }

    /**
     * Merge two lists according to specified strategy
     * @param source Original list
     * @param target Target list (may contain strategy marker)
     * @param parentStrategy Strategy inherited from parent context
     * @return Merged list
     */
    private List mergeLists(List source, List target, MergeStrategy parentStrategy) {
        def (strategy, cleanedTarget) = extractStrategy(target)
        strategy = strategy ?: parentStrategy ?: MergeStrategy.REPLACE

        switch (strategy) {
            case MergeStrategy.REPLACE: return cleanedTarget
            case MergeStrategy.APPEND: return appendMerge(source, cleanedTarget, strategy)
            case MergeStrategy.UNIQUE: return uniqueMerge(appendMerge(source, cleanedTarget, strategy))
            case MergeStrategy.DEEP_MERGE: return deepMergeLists(source, cleanedTarget, strategy)
            case MergeStrategy.KEY_BASED: return keyBasedMerge(source, cleanedTarget, strategy)
            default: throw new UnsupportedOperationException("Unhandled strategy: $strategy")
        }
    }

    /**
     * Extract merge strategy from list and clean target elements
     * @param list Input list potentially containing strategy marker
     * @return Tuple containing [detected strategy, cleaned list]
     */
    private Tuple2<MergeStrategy, List> extractStrategy(List list) {
        MergeStrategy strategy = null
        List cleanedList = list.findAll { elem ->
            if (elem instanceof Map && elem['@merge']) {
                strategy = MergeStrategy.from(elem['@merge'].toString())
                false  // Remove strategy marker from actual content
            } else {
                true   // Keep regular elements
            }
        }
        return new Tuple2(strategy, cleanedList)
    }

    /**
     * Append merge strategy implementation
     * @param source Original list
     * @param target Target list to append/merge
     * @param strategy Active merge strategy
     * @return Merged list with element-wise merging
     */
    private List appendMerge(List source, List target, MergeStrategy strategy) {
        List merged = new ArrayList(source)
        target.eachWithIndex { targetVal, index ->
            merged[index < merged.size()
                    ? index
                    : merged.size()] = mergeValues(merged[index], targetVal, strategy)
        }
        return merged
    }

    /**
     * Deduplicate list elements while preserving order
     * @param list List to process
     * @return List with duplicate scalar values removed
     */
    private List uniqueMerge(List list) {
        List unique = []
        Set seen = new HashSet()
        list.each { element ->
            if (element instanceof List || element instanceof Map) {
                unique.add(element)  // Complex elements are not deduplicated
            } else if (!seen.contains(element)) {
                seen.add(element)
                unique.add(element)
            }
        }
        return unique
    }

    /**
     * Deep merge implementation for nested structures
     * @param source Original list
     * @param target Target list to merge
     * @param strategy Active merge strategy
     * @return Deep-merged list
     */
    private List deepMergeLists(List source, List target, MergeStrategy strategy) {
        keyBasedMerge(source, target, strategy)
    }

    /**
     * Key-based merge implementation using identifier fields
     * @param source Original list
     * @param target Target list to merge
     * @param strategy Active merge strategy
     * @return Merged list using identifier keys
     */
    private List keyBasedMerge(List source, List target, MergeStrategy strategy) {
        List merged = new ArrayList(source)
        target.each { targetElement ->
            def key = findIdentifierKey(targetElement)
            def sourceElement = key ? merged.find { findIdentifierKey(it) == key } : null
            sourceElement
                    ? merged[merged.indexOf(sourceElement)] = mergeValues(sourceElement, targetElement, strategy)
                    : merged.add(targetElement)
        }
        return merged
    }

    /**
     * Find identifier key value in map elements
     * @param element List element to inspect
     * @return Value of first found identifier field (id/name) or null
     */
    private def findIdentifierKey(element) {
        return (element instanceof Map)
                ? element.findResult { k, v -> ['id', 'name'].contains(k) ? v : null }
                : null
    }
}