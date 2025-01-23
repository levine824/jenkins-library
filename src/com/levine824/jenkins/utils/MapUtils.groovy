package com.levine824.jenkins.utils

import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

class MapUtils {

    /**
     * Splits the strings in the set and use keys to get the value,
     * then returns this map.
     *
     * @param m a {@code Map}
     * @param s
     * @param delimiter
     * @return the {@code Map}
     */
    static Map get(Map m, Set<String> s, String delimiter) {
        Map map = [:]
        s.each { str ->
            Object value = get(m, str.split(Pattern.quote(delimiter)))
            map.put(str, value)
        }
        return map
    }

    /**
     * Iterates through the given keys and returns the value
     * to which the specified keys are mapped.
     *
     * @param m a {@code Map}
     * @param keys
     * @return the value
     */
    static Object get(Map m, String... keys) {
        return keys.inject(m) { value, key ->
            return value instanceof Map ? value.get(key) : null
        }
    }

    /**
     * Merges given maps which nest with the {@code Map} or the {@code List}.
     *
     * @param m1 the map as the base map
     * @param m2 the map merged into the base map
     * @return the merged {@code Map}
     */
    static Map merge(Map m1, Map m2) {
        return m2.inject((Map) m1.clone()) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = merge((Map) map[entry.key], (Map) entry.value)
            } else if (map[entry.key] instanceof List && entry.value instanceof List) {
                // just combine elements of two lists and remove duplicate elements
                Stream s1 = ((List) map[entry.key]).stream()
                Stream s2 = ((List) entry.value).stream()
                map[entry.key] = Stream.concat(s1, s2).distinct().collect(Collectors.toList())
            } else {
                map[entry.key] = entry.value
            }
            return map
        }
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
