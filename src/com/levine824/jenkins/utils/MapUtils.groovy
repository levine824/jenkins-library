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
        get(map, keys)
    }

    static Object get(Map map, String... keys) {
        if (!map || !keys) return null
        Object current = map
        for (key in keys) {
            if (current == null) return null
            Matcher matcher = (key =~ KEY_WITH_INDEX_PATTERN)
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid key format: ${key}")
            }
            String baseKey = matcher.group(1)?.trim() ?: null
            String indexStr = matcher.group(2)
            List<Integer> indices = indexStr.findAll(INDEX_PATTERN)*.toInteger()
            if (baseKey != null) {
                current = current instanceof Map ? current.get(baseKey) : null
            }
            if (indices) {
                if (current instanceof List) {
                    for (int i in indices) {
                        if (current == null) break
                        current = (current instanceof List && i >= 0 && i < current.size())
                                ? current[i]
                                : null
                    }
                } else {
                    current = null
                }
            }
        }
        return current
    }
}