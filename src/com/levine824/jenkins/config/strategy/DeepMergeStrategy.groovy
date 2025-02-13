package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy
import com.levine824.jenkins.config.merger.MergeStrategyRegistry

class DeepMergeStrategy implements MergeStrategy {
    public static final String STRATEGY_KEY = "@merge"

    private List<String> uniqueKeys

    DeepMergeStrategy(List<String> uniqueKeys = ["id", "name"]) {
        this.uniqueKeys = uniqueKeys
    }

    @Override
    Object merge(Object baseConfig, Object customConfig) {
        String strategy = getStrategy(customConfig)
        return strategy
                ? MergeStrategyRegistry.getStrategy(strategy).merge(baseConfig, customConfig)
                : mergeRecursive(baseConfig, customConfig)
    }

    @Override
    String getName() {
        return "deep"
    }

    private static String getStrategy(Object config) {
        if (config instanceof Map) {
            return config[STRATEGY_KEY]
        } else if (config instanceof List) {
            Map m = config.find { element ->
                element instanceof Map && element.containsKey(STRATEGY_KEY)
            }
            // TODO: map的size大于一会丢失数据
            return m[STRATEGY_KEY]
        }
    }

    private Object mergeRecursive(Object baseConfig, Object customConfig) {
        if (baseConfig instanceof Map && customConfig instanceof Map) {
            return mergeMap(baseConfig, customConfig)
        } else if (baseConfig instanceof List && customConfig instanceof List) {
            return mergeList(baseConfig, customConfig)
        }
        return customConfig
    }

    private Map mergeMap(Map baseConfig, Map customConfig) {
        Map mergedConfig = baseConfig.getClass().getDeclaredConstructor().newInstance()
        mergedConfig.putAll(baseConfig)
        customConfig.each { key, value ->
            mergedConfig[key] = mergedConfig.containsKey(key)
                    ? merge(mergedConfig[key], value)
                    : value
        }
        return mergedConfig
    }


    private Map mergeList(List baseConfig, List customConfig) {
        List mergedConfig = baseConfig.getClass().getDeclaredConstructor().newInstance()
        mergedConfig.addAll(baseConfig)
        customConfig.each { customElement ->
            String uniqueKey = customElement instanceof Map
                    ? findKey(customElement, uniqueKeys)
                    : null
            Object baseElement = uniqueKey
                    ? baseConfig.find { element -> findKey(element, [uniqueKey]) && customElement[uniqueKey] == element }
                    : null
            if (baseElement) {
                int index = mergedConfig.indexOf(baseElement)
                mergedConfig[index] = merge(baseElement, customElement)
            } else {
                mergedConfig << customElement
            }
        }
        return mergedConfig
    }

    private static String findKey(Object element, List<String> keys) {
        return (element instanceof Map)
                ? keys.find { element.containsKey(it) }
                : null
    }
}
