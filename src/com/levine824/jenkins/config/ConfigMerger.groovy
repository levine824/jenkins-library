package com.levine824.jenkins.config

class ConfigMerger {

    static Object merge(Map base, Map custom) {
        merge(base, custom, new MergeOptions())
    }

    static Object merge(Map base, Map custom, MergeOptions options) {
        mergeObjects(base, custom, null, options)
    }

    private static Object mergeObjects(
            Object base, Object custom, MergeStrategy parentStrategy, MergeOptions options) {
        if (base instanceof Map && custom instanceof Map) {
            return mergeMaps((Map) base, (Map) custom, parentStrategy, options)
        } else if (base instanceof List && custom instanceof List) {
            return mergeLists((List) base, (List) custom, parentStrategy, options)
        } else {
            if (base != null && custom != null && base.getClass() != custom.getClass()) {
                throw new IllegalArgumentException("Cannot merge different types: " +
                        "${base.getClass().simpleName} and ${custom.getClass().simpleName}")
            }
            return custom != null ? custom : base
        }
    }

    private static Map mergeMaps(
            Map base, Map custom, MergeStrategy parentStrategy, MergeOptions options) {
        Map merged = new HashMap(base)
        custom.each { key, value ->
            merged[key] = merged.containsKey(key)
                    ? mergeObjects(merged[key], value, parentStrategy, options)
                    : value
        }
        return merged
    }

    private static List mergeLists(
            List base, List custom, MergeStrategy parentStrategy, MergeOptions options) {
        def (strategy, cleanedTarget) = extractStrategy(custom)
        strategy = resolveStrategy(strategy, parentStrategy, options.defaultMergeStrategy)
        switch (strategy) {
            case MergeStrategy.REPLACE:
                return cleanedTarget
            case MergeStrategy.APPEND:
                return appendMerge(base, cleanedTarget)
            case MergeStrategy.UNIQUE:
                return uniqueMerge(base, cleanedTarget)
            case MergeStrategy.DEEP_MERGE:
                return deepMerge(base, cleanedTarget, strategy, options)
            case MergeStrategy.KEY_BASED:
                return keyBasedMerge(base, cleanedTarget, strategy, options)
            default:
                throw new UnsupportedOperationException("Unhandled strategy: $strategy")
        }
    }

    private static Tuple2<MergeStrategy, List> extractStrategy(List list) {
        MergeStrategy strategy = null
        List cleanedList = []
        list.each { element ->
            if (element instanceof Map && element.containsKey('@merge')) {
                if (strategy != null) {
                    throw new IllegalArgumentException("Multiple @merge strategies defined in list")
                }
                strategy = parseStrategy(element['@merge'])
            } else {
                cleanedList << element
            }
        }

        return new Tuple2(strategy, cleanedList)
    }

    private static MergeStrategy parseStrategy(Object strategy) {
        return MergeStrategy.from(strategy.toString())
    }

    private static MergeStrategy resolveStrategy(
            MergeStrategy current, MergeStrategy parent, MergeStrategy defaultStrategy) {
        return current ?: parent ?: defaultStrategy
    }

    private static List appendMerge(List base, List custom) {
        return base + custom
    }

    private static List uniqueMerge(List base, List custom) {
        return (base + custom).unique()
    }

    private static List deepMerge(
            List base, List custom, MergeStrategy strategy, MergeOptions options) {
        return keyBasedMerge(base, custom, strategy, options)
    }

    private static List keyBasedMerge(
            List base, List custom, MergeStrategy strategy, MergeOptions options) {
        List merged = new ArrayList(base)
        custom.each { targetElement ->
            def targetKey = findIdentifierKey(targetElement, options.identifierKeys)
            def sourceElement = findMatchingElement(merged, targetKey)
            if (sourceElement) {
                int index = merged.indexOf(sourceElement)
                merged[index] = (strategy == MergeStrategy.DEEP_MERGE)
                        ? mergeObjects(sourceElement, targetElement, strategy, options)
                        : targetElement
            } else {
                merged << targetElement
            }
        }
        return merged
    }

    private static Tuple2<String, Object> findIdentifierKey(element, List<String> identifierKeys) {
        if (element instanceof Map) {
            for (key in identifierKeys) {
                if (element.containsKey(key)) {
                    return new Tuple2(key, element[key])
                }
            }
        }
        return null
    }

    private static Object findMatchingElement(List merged, Tuple2<String, Object> targetKey) {
        if (!targetKey) return null
        merged.find { sourceElement ->
            def sourceKey = findIdentifierKey(sourceElement, [targetKey.v1])
            sourceKey?.v2 == targetKey.v2
        }
    }
}