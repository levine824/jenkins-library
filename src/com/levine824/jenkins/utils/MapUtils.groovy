package com.levine824.jenkins.utils

import java.util.regex.Matcher

/**
 * A utility class providing methods for handling nested Maps, including retrieving nested values,
 * flattening nested structures, and converting to environment variable format.
 */
class MapUtils {

    /**
     * Retrieves a nested value from a Map using a path string with specified separator.
     *
     * @param map The nested Map structure to search. Returns null if null or empty.
     * @param path A dot-separated (or custom separator) path string (e.g., "a.b.c").
     * @param separator The separator used in the path (default: ".").
     * @return The value at the nested path, or null if any key in the path is invalid.
     */
    static Object getNestedValue(
            Map<String, Object> map,
            String path,
            String separator = "."
    ) {
        if (!map || !path) return null
        String escapedSep = StringUtils.escape(separator)
        String[] keys = path.split(escapedSep)
        return getNestedValue(map, keys)
    }

    /**
     * Retrieves a nested value from a Map using an array of keys.
     * Supports keys with array index notation (e.g., "key[0]").
     *
     * @param map The nested Map structure to search. Returns null if null or empty.
     * @param keys Array of keys to traverse the nested structure.
     * @return The value at the nested path, or null if any key is invalid.
     */
    static Object getNestedValue(Map<String, Object> map, String... keys) {
        if (!map || !keys) return null
        return keys.inject(map) { value, key ->
            if (value == null) return null
            def (cleanKey, index) = parseKeyAndIndex(key)
            switch (value) {
                case Map:
                    return getValueAt(value[cleanKey], index)
                case List:
                    return getValueAt(value, index)
                default:
                    return null
            }
        }
    }

    /**
     * Converts a nested Map into a list of environment variable strings.
     * Flattens the Map and formats keys with the specified separator (default: "_").
     * Example: "parent.key" becomes "PARENT_KEY=value".
     *
     * @param map The nested Map to convert.
     * @param separator Separator for flattened keys (default: "_").
     * @return List of environment variable strings, or empty list if input is null.
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
     * Flattens a nested Map into a single-level Map with compound keys.
     * Example: {a: {b: 1}} becomes {"a.b": 1}.
     * Handles Lists by appending indices to keys (e.g., "list[0]").
     *
     * @param map The nested Map to flatten.
     * @param prefix Initial prefix for keys (used internally in recursion).
     * @param separator Separator for joining keys (default: ".").
     * @return Flattened Map, or empty Map if input is null.
     */
    static Map<String, Object> flatten(
            Map<String, Object> map,
            String prefix = "",
            String separator = "."
    ) {
        if (!map) return [:]
        Map<String, Object> flatMap = new HashMap<String, Object>()
        map.each { key, value ->
            String currentKey = prefix
                    ? "${prefix}${separator}${key}".toString()
                    : key
            if (value instanceof Map) {
                flatMap.putAll(flatten(value, currentKey, separator))
            } else if (value instanceof List) {
                value.eachWithIndex { element, index ->
                    String listKey =
                            "${currentKey}${separator}${index}".toString()
                    if (element instanceof Map) {
                        flatMap.putAll(flatten(element, listKey, separator))
                    } else if (element instanceof List) {
                        Map<String, Object> m = [(index.toString()): element]
                        flatMap.putAll(flatten(m, listKey, separator))
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

    /**
     * Parses a key string to extract the base key and array index (if present).
     * Example: "key[3]" returns ["key", 3]. Non-indexed keys return index -1.
     *
     * @param rawKey The key string to parse.
     * @return Tuple2 containing the cleaned key and index (or -1 if none).
     */
    private static Tuple2<String, Integer> parseKeyAndIndex(String rawKey) {
        Matcher matcher = (rawKey =~ /^([^\[]*)\[(\d+)\]$/)
        return matcher.matches()
                ? [matcher.group(1), matcher.group(2) as Integer]
                : [rawKey, -1]
    }

    /**
     * Safely retrieves a value from a List by index.
     *
     * @param value The target object (expected to be a List).
     * @param index The index to retrieve. Returns value directly if index is -1.
     * @return The element at the index, null if invalid index or non-List input.
     */
    private static Object getValueAt(Object value, int index) {
        if (index == -1) return value
        if (value instanceof List && index in 0..<value.size()) {
            return value[index]
        }
        return null
    }
}