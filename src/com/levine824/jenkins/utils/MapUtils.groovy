package com.levine824.jenkins.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

class MapUtils {

    static List toEnvVars(Map m, String separator = '_') {
        if (!m) return null
        flatten(m, '', separator).collect { key, value ->
            String envKey = StringUtils.toEnvKey(key.toString(), separator)
            String envVal = value?.toString()
            "${envKey}=${envVal}".toString()
        }
    }

    static Map flatten(Object o, String prefix = '', String separator = '.') {
        Map flat = [:]
        if (o instanceof Map) {
            o.each { key, value ->
                String currentKey = prefix ? "${prefix}${separator}${key}" : key.toString()
                flat.putAll(flatten(value, currentKey, separator))
            }
        } else if (o instanceof List) {
            o.eachWithIndex { element, index ->
                String currentKey = prefix ? "${prefix}[${index}]" : "[${index}]"
                flat.putAll(flatten(element, currentKey, separator))
            }
        } else if (prefix) {
            flat[prefix] = o
        }
        return flat
    }

    static Object getByPath(Map m, String path, String separator = '.') {
        if (!m || !path) return null
        String escapedSep = Pattern.quote(separator)
        String[] keys = path.split(escapedSep)
        getByKeys(m, keys)
    }

    static Object getByKeys(Map m, String... keys) {
        if (!m || !keys) return null
        keys.inject(m) { value, key ->
            if (value == null) return null
            IndexedKey indexedKey = IndexedKey.parse(key)
            if (!indexedKey) {
                throw new IllegalArgumentException("Invalid key format: ${key}")
            }
            if (indexedKey.baseKey != null) {
                if (value instanceof Map) {
                    value = value.get(indexedKey.baseKey)
                } else {
                    return null
                }
            }
            if (indexedKey.indices) {
                if (value instanceof List) {
                    value = getByIndices(value, indexedKey.indices)
                } else {
                    return null
                }
            }
            return value
        }
    }

    private static Object getByIndices(List l, List<Integer> indices) {
        indices.inject(l) { value, index ->
            if (value == null || !(value instanceof List)) return null
            if (index >= 0 && index < value.size()) {
                return value[index]
            } else {
                return null
            }
        }
    }

    private static class IndexedKey {
        static final Pattern KEY_WITH_INDEX_PATTERN = ~/^([a-zA-Z_]\w*)?((?:\[\d+\])*)$/
        static final Pattern INDEX_PATTERN = ~/\d+/

        String baseKey
        List<Integer> indices

        static IndexedKey parse(String key) {
            Matcher matcher = key =~ KEY_WITH_INDEX_PATTERN
            if (!matcher.matches()) return null
            String baseKey = matcher.group(1)?.trim() ?: null
            List<Integer> indices = matcher.group(2).findAll(INDEX_PATTERN)*.toInteger()
            return new IndexedKey(baseKey: baseKey, indices: indices)
        }
    }
}