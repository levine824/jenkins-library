package com.levine824.config.merger.strategy

class DeepMergeStrategy extends AbstractMergeStrategy {
    private List<String> uniqueKeys
    private Closure handler

    DeepMergeStrategy(Map options = [:]) {
        this.uniqueKeys = options.uniqueKeys as List ?: ["id", "name"]
        this.handler = options.handler as Closure ?: { b, c -> merge(b, c) }
    }

    @Override
    Map mergeMaps(Map baseConfig, Map customConfig) {
        def mergedConfig = baseConfig + [:]
        customConfig.each { key, value ->
            mergedConfig[key] = mergedConfig.containsKey(key)
                    ? handler(mergedConfig[key], value)
                    : value
        }
        return mergedConfig
    }

    @Override
    List mergeLists(List baseConfig, List customConfig) {
        def mergedConfig = baseConfig + []
        def uniqueKeyIndex = buildUniqueKeyIndex(baseConfig)
        customConfig.each {
            if (it instanceof Map) {
                def uniqueKey = findUniqueKey(it)
                if (uniqueKey) {
                    def key = "${uniqueKey}:${it[uniqueKey]}".toString()
                    def index = uniqueKeyIndex[key]
                    if (index != null) {
                        mergedConfig[index] = handler.call(baseConfig[index], it)
                    } else {
                        mergedConfig << handler(null, it)
                    }
                } else {
                    mergedConfig << handler(null, it)
                }
            } else {
                mergedConfig << handler(null, it)
            }
        }
        return mergedConfig
    }

    private Map<String, Integer> buildUniqueKeyIndex(List baseConfig) {
        Map<String, Integer> uniqueKeyIndex = [:]
        baseConfig.eachWithIndex { element, index ->
            if (element instanceof Map) {
                def uniqueKey = findUniqueKey(element)
                if (uniqueKey) {
                    def key = "${uniqueKey}:${element[uniqueKey]}".toString()
                    uniqueKeyIndex[key] = index
                }
            }
        }
        return uniqueKeyIndex
    }

    private String findUniqueKey(Object o) {
        return o instanceof Map
                ? uniqueKeys.find { o.containsKey(it) }
                : null
    }
}
