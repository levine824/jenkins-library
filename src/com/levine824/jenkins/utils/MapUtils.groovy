package com.levine824.jenkins.utils

import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

class MapUtils {

    static Map get(Map m, Set<String> s, String delimiter) {
        Map map = [:]
        s.each { str ->
            Object value = get(m, str.split(Pattern.quote(delimiter)))
            map.put(str, value)
        }
        return map
    }

    static Object get(Map m, String... keys) {
        return keys.inject(m) { value, key ->
            return value instanceof Map ? value.get(key) : null
        }
    }

    /**
     * Merges given maps which nest with the {@code Map} or the {@code List}.
     *
     * @param lhs the map to be merged, better be the bigger one
     * @param rhs the map to be merged
     * @return the merged {@code Map}
     */
    static Map merge(Map lhs, Map rhs) {
        return mergeMap(lhs, rhs)
    }

    private static Map mergeMap(Map lhs, Map rhs) {
        return rhs.inject((Map) lhs.clone()) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = mergeMap((Map) map[entry.key], (Map) entry.value)
            } else if (map[entry.key] instanceof List && entry.value instanceof List) {
                map[entry.key] = mergeList((List) map[entry.key], (List) entry.value)
            } else {
                map[entry.key] = entry.value
            }
            return map
        }
    }

    private static List mergeList(List lhs, List rhs) {
        // just combine elements of two lists and remove duplicate elements
        return Stream.concat(lhs.stream(), rhs.stream()).distinct().collect(Collectors.toList())
    }

    /**
     * Iterates and concatenates all keys with the specified string.
     *
     * @param m a {@code Map}
     * @param prefix the string added to the beginning of all keys
     * @param delimiter the delimiter for concatenating keys
     * @return the flat {@code Map}
     */
    static Map flatten(Map m, String prefix, String delimiter) {
        Map flatMap = [:]
        m.collectEntries { key, value ->
            String newKey = prefix ? prefix + delimiter + key : key.toString()
            if (value instanceof Map) {
                flatMap.putAll(flatten(value, newKey, delimiter))
            } else {
                flatMap.put(newKey, value)
            }
        }
        return flatMap
    }

    /**
     * Converts all keys to the environment variable case.
     *
     * @param m a {@code Map}
     * @return the converted {@code Map}
     */
    static toEnvCase(Map m) {
        return m.collectEntries { key, value ->
            [(StringUtils.toEnvCase(key.toString())): value]
        }
    }

}
