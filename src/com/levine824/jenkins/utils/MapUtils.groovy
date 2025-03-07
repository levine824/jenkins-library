package com.levine824.jenkins.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

class MapUtils {
    private static final Pattern KEY_WITH_INDEX_PATTERN = ~/^([a-zA-Z_]\w*)?((?:\[\d+\])*)$/
    private static final Pattern INDEX_PATTERN = ~/\d+/

    static Map flatten(Object obj, String prefix = '',
                       String separator = '.', boolean indexList = false) {
        Map flat = [:]
        if (obj instanceof Map) {
            obj.each { key, value ->
                String newKey = prefix
                        ? "${prefix}${separator}${key}"
                        : key?.toString()
                flat.putAll(flatten(value, newKey, separator))
            }
        } else if (obj instanceof List) {
            if (indexList) {
                obj.eachWithIndex { element, index ->
                    String newKey = prefix
                            ? "${prefix}[${index}]"
                            : index?.toString()
                    flat.putAll(flatten(element, newKey, separator, indexList))
                }
            } else {
                flat[prefix] = obj
            }
        } else if (prefix) {
            flat[prefix] = obj
        }

        return flat
    }

    static Object getByPath(Map map, String path, String separator = '.') {
        if (!map || !path) return null
        String escapedSep = Pattern.quote(separator)
        String[] keys = path.split(escapedSep)
        getByKeys(map, keys)
    }

    static Object getByKeys(Map map, String... keys) {
        if (!map || !keys) return null
        keys.inject(map) { value, key ->
            if (value == null) return null
            Matcher matcher = (key =~ KEY_WITH_INDEX_PATTERN)
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid key format: ${key}")
            }
            String baseKey = matcher.group(1)?.trim() ?: null
            String indexStr = matcher.group(2)
            List<Integer> indices = indexStr.findAll(INDEX_PATTERN)*.toInteger()
            if (baseKey != null) {
                value = value instanceof Map ? value.get(baseKey) : null
            }
            if (indices && value instanceof List) {
                value = indices.inject(value) { v, i ->
                    v instanceof List && i >= 0 && i < v.size() ? v[i] : null
                }
            } else if (indices) {
                value = null
            }
            return value
        }
    }
}