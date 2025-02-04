package com.levine824.jenkins.utils

/**
 * A utility class for manipulating and querying nested Map structures.
 * Provides functionality for deep value retrieval, map flattening, and environment variable conversion.
 * Supports handling of nested Maps and Lists with index-based access syntax (e.g., "key[0]").
 */
class MapUtils {
    /**
     * Retrieves a nested value from a Map structure using a path string with a specified separator.
     * Supports accessing list elements via bracket notation (e.g., "key[1]").
     *
     * @param map The source Map containing nested data
     * @param path Dot-separated path string (e.g., "user.address[0].street")
     * @param separator Optional path separator (default: ".")
     * @return The found value or null if any level doesn't exist
     */
    static Object getNestedValue(Map<String, Object> map, String path, String separator = ".") {
        if (!map || !path) return null
        def escapedSep = StringUtils.escapeRegex(separator)
        def keys = path.split(escapedSep)
        return getNestedValue(map, keys)
    }

    /**
     * Retrieves a nested value from a Map structure using direct key sequence.
     * Handles both Map and List structures during traversal.
     *
     * @param map The source Map containing nested data
     * @param keys Sequence of keys to traverse (may contain array indices)
     * @return The found value or null if any level doesn't exist
     */
    static Object getNestedValue(Map<String, Object> map, String... keys) {
        if (!map || !keys) return null
        return keys.inject(map) { value, key ->
            if (value == null) return null
            def (actualKey, index) = parseKeyWithIndex(key)
            switch (value) {
                case Map:
                    return resolveIndexedValue(value[actualKey], index)
                case List:
                    return resolveIndexedValue(value, index)
                default:
                    return null
            }
        }
    }

    /**
     * Converts a nested Map structure into environment variable format (KEY=VAL strings).
     *
     * @param map The Map to convert. If null, returns an empty list.
     * @param separator The delimiter for nested key composition (default is "_").
     * @return A list of environment variable strings with uppercase keys (e.g., ["ROOT_A_B=value"]).
     */
    static List<String> toEnvVars(Map<String, Object> map, String separator = "_") {
        if (!map) return []
        return flatten(map, "", separator).collect { key, value ->
            def envKey = StringUtils.toEnvKey(key, separator)
            def envVal = value?.toString()
            "${envKey}=${envVal}".toString()
        }
    }

    /**
     * Flattens a nested Map structure into a single-level Map with compound keys.
     *
     * @param map The nested Map to flatten. If null, returns an empty Map.
     * @param prefix The base prefix for key composition (used internally during recursion).
     * @param separator The delimiter for nested key composition (default is ".").
     * @return A flattened Map where keys represent traversal paths (e.g., "root.child[0].value").
     */
    static Map<String, Object> flatten(Map<String, Object> map, String prefix = "", String separator = ".") {
        if (!map) return [:]
        Map<String, Object> flatMap = new HashMap<String, Object>()
        map.each { key, value ->
            def currentKey = prefix ? "${prefix}${separator}${key}".toString() : key
            if (value instanceof Map) {
                flatMap.putAll(flatten(value, currentKey, separator))
            } else if (value instanceof List) { // TODO: need to flat List? or just keep original
                value.eachWithIndex { element, index ->
                    def listKey = "${currentKey}${separator}${index}".toString()
                    if (element instanceof Map) {
                        flatMap.putAll(flatten(element, listKey, separator))
                    } else if (element instanceof List) {
                        // Convert sublist to pseudo-map for recursive processing
                        flatMap.putAll(flatten([(index.toString()): element], listKey, separator))
                    } else {
                        flatMap[listKey] = element
                    }
                }
            } else {
                flatMap[currentKey] = value
            }
        }
        return flatMap
    }

    private static Tuple2<String, Integer> parseKeyWithIndex(String rawKey) {
        def matcher = (rawKey =~ /^([^\[]*)\[(\d+)\]$/)
        return matcher.matches()
                ? [matcher.group(1), matcher.group(2) as Integer]
                : [rawKey, -1]
    }


    private static Object resolveIndexedValue(Object value, int index) {
        if (index == -1) return value
        if (value instanceof List && index in 0..<value.size()) {
            return value[index]
        }
        return null
    }
}
