package com.levine824.jenkins.config

class ConfigMerger {

    /**
     * Merge strategy enumeration:
     * - REPLACE: Completely override base value
     * - APPEND: Concatenate list elements
     * - UNIQUE: Merge lists with duplicate removal
     * - DEEP_MERGE: Recursively merge nested structures
     * - KEY_BASED: Merge using element's 'key' property
     */
    enum MergeStrategy {
        REPLACE,
        APPEND,
        UNIQUE,
        DEEP_MERGE,
        KEY_BASED
    }

    /**
     * Merges two configuration maps
     * @param baseConfig Base configuration map
     * @param overrideConfig Override map with strategy markers
     * @return New merged configuration map
     */
    static Map<String, Object> merge(
            Map<String, Object> baseConfig, Map<String, Object> overrideConfig) {
        return mergeNestedMaps(baseConfig ?: [:], overrideConfig ?: [:])
    }

    private static Map<String, Object> mergeNestedMaps(
            Map<String, Object> baseMap, Map<String, Object> overrideMap) {
        def mergedResult = new HashMap<String, Object>(baseMap)
        overrideMap.each { rawKey, overrideValue ->
            def (cleanKey, strategy) = parseStrategyMarker(rawKey.toString())
            def baseValue = mergedResult[cleanKey]
            mergedResult[cleanKey] = baseValue != null
                    ? applyMergeStrategy(baseValue, overrideValue, strategy)
                    : overrideValue
        }
        return mergedResult
    }

    /**
     * Extracts strategy marker from key
     * @param rawKey Original key with optional strategy suffix
     * @return Tuple containing cleaned key and detected strategy
     * @throws IllegalArgumentException For unknown strategy markers
     */
    private static Tuple2<String, MergeStrategy> parseStrategyMarker(String rawKey) {
        def markerMatcher = rawKey =~ /^(.*?)([!+*^~])$/
        if (markerMatcher.matches()) {
            String baseKey = markerMatcher.group(1)
            String marker = markerMatcher.group(2)
            def strategy = switch (marker) {
                case '!' -> MergeStrategy.REPLACE
                case '+' -> MergeStrategy.APPEND
                case '*' -> MergeStrategy.UNIQUE
                case '^' -> MergeStrategy.DEEP_MERGE
                case '~' -> MergeStrategy.KEY_BASED
                default -> throw new IllegalArgumentException("Unknown strategy marker: ${marker}")
            }
            return new Tuple2(baseKey, strategy)
        }
        return new Tuple2(rawKey, MergeStrategy.DEEP_MERGE)
    }

    /**
     * Applies merge strategy to values
     * @throws IllegalArgumentException When list strategies applied to non-list types
     */
    private static Object applyMergeStrategy(
            Object baseValue, Object overrideValue, MergeStrategy strategy) {
        if (baseValue instanceof Map && overrideValue instanceof Map) {
            return mergeNestedMaps((Map) baseValue, (Map) overrideValue)
        }

        if (baseValue instanceof List && overrideValue instanceof List) {
            return mergeListStrategies((List) baseValue, (List) overrideValue, strategy)
        }

        if (strategy in [MergeStrategy.APPEND, MergeStrategy.UNIQUE, MergeStrategy.KEY_BASED]) {
            throw new IllegalArgumentException("Strategy '${strategy}' requires both values to be lists")
        }
        return overrideValue
    }

    private static List<Object> mergeListStrategies(
            List<Object> baseList, List<Object> overrideList, MergeStrategy strategy) {
        switch (strategy) {
            case MergeStrategy.REPLACE:
                return handleListReplacement(overrideList)
            case MergeStrategy.APPEND:
                return concatenateLists(baseList, overrideList)
            case MergeStrategy.UNIQUE:
                return mergeUniqueLists(baseList, overrideList)
            case MergeStrategy.DEEP_MERGE:
                return deepMergeNestedLists(baseList, overrideList)
            case MergeStrategy.KEY_BASED:
                return mergeKeyedLists(baseList, overrideList)
            default:
                throw new UnsupportedOperationException("Unsupported strategy: ${strategy}")
        }
    }

    private static List<Object> handleListReplacement(List<Object> overrideList) {
        return new ArrayList<>(overrideList)
    }

    private static List<Object> concatenateLists(List<Object> baseList, List<Object> overrideList) {
        return baseList + overrideList
    }

    private static List<Object> mergeUniqueLists(List<Object> baseList, List<Object> overrideList) {
        LinkedHashSet<Object> uniqueElements = new LinkedHashSet<>(baseList.size() + overrideList.size())
        List<Object> result = new ArrayList<>()
        (baseList + overrideList).each {
            if (uniqueElements.add(it)) result.add(it)
        }
        return result
    }

    private static List<Object> deepMergeNestedLists(List<Object> baseList, List<Object> overrideList) {
        List<Object> mergedList = new ArrayList<>(Math.max(baseList.size(), overrideList.size()))
        int maxLength = Math.max(baseList.size(), overrideList.size())
        (0..<maxLength).each { index ->
            mergedList.add(mergeElementsByIndex(
                    index < baseList.size() ? baseList[index] : null,
                    index < overrideList.size() ? overrideList[index] : null
            ))
        }
        return mergedList
    }

    private static Object mergeElementsByIndex(Object baseElement, Object overrideElement) {
        if (overrideElement == null) return baseElement
        if (baseElement == null) return overrideElement

        if (baseElement instanceof Map && overrideElement instanceof Map) {
            return mergeNestedMaps((Map) baseElement, (Map) overrideElement)
        }
        if (baseElement instanceof List && overrideElement instanceof List) {
            return deepMergeNestedLists((List) baseElement, (List) overrideElement)
        }
        return overrideElement
    }

    private static List<Object> mergeKeyedLists(List<Object> baseList, List<Object> overrideList) {
        Map<Object, Object> keyedElements = new LinkedHashMap<>()
        populateKeyedElements(baseList, keyedElements)
        populateKeyedElements(overrideList, keyedElements)
        return new ArrayList<>(keyedElements.values())
    }

    private static void populateKeyedElements(List<Object> elementList, Map<Object, Object> targetMap) {
        elementList.eachWithIndex { element, index ->
            validateKeyedElementStructure(element, index)
            Map<String, Object> mapElement = (Map) element
            Object elementKey = mapElement['key']
            targetMap[elementKey] = targetMap.containsKey(elementKey)
                    ? mergeNestedMaps(targetMap[elementKey], mapElement)
                    : mapElement
        }
    }

    private static void validateKeyedElementStructure(Object element, int index) {
        if (!(element instanceof Map)) {
            throw new IllegalArgumentException(
                    "Element at index ${index} must be Map, found: ${element?.getClass()?.simpleName}"
            )
        }
        if (!((Map) element).containsKey('key')) {
            throw new IllegalArgumentException(
                    "Element at index ${index} missing required 'key' field: ${element}"
            )
        }
    }
}