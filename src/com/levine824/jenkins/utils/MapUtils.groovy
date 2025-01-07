package com.levine824.jenkins.utils

import java.util.stream.Collectors
import java.util.stream.Stream

class MapUtils {

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
        return rhs.inject(lhs.clone() as Map) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = mergeMap(map[entry.key] as Map, entry.value as Map)
            } else if (map[entry.key] instanceof List && entry.value instanceof List) {
                mergeList(map[entry.key] as List, entry.value as List)
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
     * Iterates and concatenates all the keys with the specified string.
     *
     * @param map a {@code Map}
     * @param prefix the string added to the beginning of all keys
     * @param delimiter the delimiter for concatenating keys
     * @return the flat {@code Map}
     */
    static Map flatten(Map map, String prefix = '', String delimiter = '.') {
        def flatMap = [:]
        map.collectEntries { key, value ->
            def newKey = prefix ? prefix + delimiter + key : key.toString()
            if (value instanceof Map) {
                flatMap.putAll(flatten(value, newKey))
            } else {
                flatMap.put(newKey, value)
            }
        }
        return flatMap
    }

    /**
     * Converts all the keys to the environment variable.
     *
     * @param map a {@code Map}
     * @return the {@code Map}, converted to to the environment variable
     */
    static Map toEnv(Map map) {
        return map.collectEntries { key, value ->
            [(StringUtils.toEnv(key.toString())): value]
        }
    }

}
