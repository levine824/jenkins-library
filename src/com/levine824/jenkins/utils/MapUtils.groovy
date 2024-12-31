package com.levine824.jenkins.utils

import java.util.stream.Collectors
import java.util.stream.Stream

class MapUtils {

    /**
     * Merge given maps which may nest with Map or List.
     *
     * @param lhs map to be merged, better be the bigger one
     * @param rhs map to be merged
     * @return merged Map
     */
    Map merge(Map lhs, Map rhs) {
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

}
