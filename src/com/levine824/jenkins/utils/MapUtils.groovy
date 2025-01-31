package com.levine824.jenkins.utils

/**
 * A utility class for manipulating and querying nested Map structures.
 * Provides functionality for deep value retrieval, map flattening, and environment variable conversion.
 * Supports handling of nested Maps and Lists with index-based access syntax (e.g., "key[0]").
 */
class MapUtils {

    /**
     * Retrieves a value from a nested Map structure using a dot-separated path string.
     *
     * @param map The nested Map to search through. If null, returns null.
     * @param path The path string specifying the traversal route (e.g., "a.b.c[0].d").
     * @param separator The delimiter for path components (default is ".").
     * @return The value found at the specified path, or null if any traversal step fails.
     */
    static Object getByPath(Map<String, Object> map, String path, String separator = ".") {
        if (!map || !path) return null
        def escapedSep = StringUtils.escapeRegex(separator)
        def keys = path.split(escapedSep)
        keys.inject(map) { value, key ->
            if (value == null) return null
            def (actualKey, index) = parseKey(key)
            switch (value) {
                case Map:
                    return getIndexedValue(value[actualKey], index)
                case List:
                    return getIndexedValue(value, index)
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
        flatten(map, "", separator).collect { key, value ->
            def envKey = StringUtils.toEnvKey(key, separator)
            def envVal = value?.toString()
            "${envKey}=${envVal}"
        } as List
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
        def flatMap = new HashMap<String, Object>()
        map.each { key, value ->
            def currentKey = prefix ? "${prefix}${separator}${key}".toString() : key
            if (value instanceof Map) {
                flatMap.putAll(flatten(value, currentKey, separator))
            } else if (value instanceof List) {
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

    private static Tuple2<String, Integer> parseKey(String rawKey) {
        def matcher = (rawKey =~ /^([^\[]*)\[(\d+)\]$/)
        matcher.matches() ? [matcher.group(1), matcher.group(2) as Integer] : [rawKey, -1]
    }


    private static Object getIndexedValue(Object value, int index) {
        if (index == -1) return value
        if (value instanceof List && index in 0..<value.size()) {
            return value[index]
        }
        return null
    }

}
