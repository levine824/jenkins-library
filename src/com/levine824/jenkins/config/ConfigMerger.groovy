package com.levine824.jenkins.config

class ConfigMerger {

    enum Strategy {
        REPLACE,     // 完全覆盖
        APPEND,      // 追加元素
        UNIQUE,      // 去重追加
        DEEP_MERGE,  // 深度合并
        KEY_BASED    // 基于键合并
    }

    static Map<String, Object> merge(
            Map<String, Object> baseMap, Map<String, Object> overrideMap) {
        return mergeMaps(baseMap, overrideMap)
    }

    private static Map<String, Object> mergeMaps(
            Map<String, Object> baseMap, Map<String, Object> overrideMap) {
        if (!baseMap) return overrideMap
        if (!overrideMap) return baseMap
        def mergedMap = new HashMap<String, Object>(baseMap)
        overrideMap.each { rawKey, overrideValue ->
            def (cleanKey, strategy) = parseKey(rawKey)
            def baseValue = mergedMap[cleanKey]
            mergedMap[cleanKey] = baseValue != null
                    ? mergeWithStrategy(baseValue, overrideValue, strategy)
                    : overrideValue
        }
        return mergedMap
    }

    private static Tuple2<String, Strategy> parseKey(String rawKey) {
        def matcher = rawKey =~ /^(.*?)([+^!])$/
        if (matcher.matches()) {
            String cleanKey = matcher.group(1)
            String mark = matcher.group(2)
            def strategy = switch (mark) {
                case '!' -> Strategy.REPLACE
                case '+' -> Strategy.APPEND
                case '*' -> Strategy.UNIQUE
                case '^' -> Strategy.DEEP_MERGE
                case '~' -> Strategy.KEY_BASED
                default -> throw new IllegalArgumentException("Unknown strategy mark: ${mark}")
            }
            return new Tuple2(cleanKey, strategy)
        }
        return new Tuple2(rawKey, Strategy.DEEP_MERGE) // 默认深度合并
    }

    private static Object mergeWithStrategy(
            Object base, Object override, Strategy strategy) {
        if (base instanceof Map && override instanceof Map) {
            return mergeMaps(base, override)
        } else if (base instanceof List && override instanceof List) {
            return mergeLists(base, override, strategy)
        }
        return override
    }

    private static List<Object> mergeLists(
            List<Object> base, List<Object> override, Strategy strategy) {
        switch (strategy) {
            case Strategy.REPLACE:
                return new ArrayList<>(override)
            case Strategy.APPEND:
                return base + override
            case Strategy.UNIQUE:
                return (base + override).unique()
            case Strategy.DEEP_MERGE:
                return deepMergeLists(base, override)
            case Strategy.KEY_BASED:
                return keyBasedMergeLists(base, override)
            default:
                throw new IllegalArgumentException("Unsupported strategy: ${strategy}")
        }
    }

    private static List<Object> deepMergeLists(List<Object> base, List<Object> override) {
        List<Object> merged = new ArrayList<>(Math.max(base.size(), override.size()))
        int maxSize = Math.max(base.size(), override.size())

        (0..<maxSize).each { index ->
            def baseElement = index < base.size() ? base[index] : null
            def overrideElement = index < override.size() ? override[index] : null

            merged.add(mergeElements(baseElement, overrideElement))
        }
        return merged
    }

    private static Object mergeElements(Object baseElement, Object overrideElement) {
        if (baseElement == null) return overrideElement
        if (overrideElement == null) return baseElement

        if (baseElement instanceof Map && overrideElement instanceof Map) {
            return mergeMaps((Map) baseElement, (Map) overrideElement)
        } else if (baseElement instanceof List && overrideElement instanceof List) {
            return deepMergeLists((List) baseElement, (List) overrideElement)
        }
        return overrideElement
    }

    private static List<Object> keyBasedMergeLists(List<Object> base, List<Object> override) {
        def mergedMap = new LinkedHashMap<Object, Object>()

        processKeyedList(base, mergedMap, "base")
        processKeyedList(override, mergedMap, "override")

        return new ArrayList<>(mergedMap.values())
    }

    private static void processKeyedList(List<Object> list, Map<Object, Object> mergedMap, String listType) {
        list.eachWithIndex { element, index ->
            validateKeyedElement(element, index, listType)

            Map<String, Object> mapElement = (Map) element
            def key = mapElement.key

            mergedMap[key] = mergedMap.containsKey(key) ?
                    mergeMaps(mergedMap[key], mapElement) :
                    mapElement
        }
    }

    private static void validateKeyedElement(Object element, int index, String listType) {
        if (!(element instanceof Map)) {
            throw new IllegalArgumentException(
                    "Invalid element in $listType list at index $index: " +
                            "Expected Map but got ${element?.getClass()?.simpleName}"
            )
        }

        if (!((Map) element).containsKey('key')) {
            throw new IllegalArgumentException(
                    "Missing 'key' in $listType list element at index $index: $element"
            )
        }
    }

}