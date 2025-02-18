package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy

class DeepMergeStrategy implements MergeStrategy {
    private List<String> uniqueKeys = ["id", "name"]

    @Override
    Object merge(Object base, Object custom) {

    }

    @Override
    String getName() {
        return "deep"
    }

    private Object deepMerge(Object base, Object custom) {
        if (base instanceof Map && custom instanceof Map) {
            return mergeMap(base, custom)
        } else if (base instanceof List && custom instanceof List) {
            return mergeList(base, custom)
        }
        return custom
    }

    private Map mergeMap(Map base, Map custom) {
        Map merged = base.getClass().getDeclaredConstructor().newInstance()
        merged.putAll(base)
        custom.each { key, value ->
            merged[key] = merged.containsKey(key)
                    ? merge(merged[key], value)
                    : value
        }
        return merged
    }

    private List mergeList(List base, List custom) {
        List merged = base.getClass().getDeclaredConstructor().newInstance()
        merged.addAll(base)
        custom.each { customElement ->
            String uniqueKey = customElement instanceof Map
                    ? findKey(customElement, uniqueKeys)
                    : null
            Object baseElement = null
            if (uniqueKey) {
                baseElement = base.find { element ->
                    element instanceof Map && customElement[uniqueKey] == element[uniqueKey]
                }
            }
            if (baseElement) {
                int index = merged.indexOf(baseElement)
                merged[index] = merge(baseElement, customElement)
            } else {
                merged << customElement
            }
        }
        return merged
    }

    private static String findKey(Object element, List<String> keys) {
        return (element instanceof Map)
                ? keys.find { element.containsKey(it) }
                : null
    }
}
