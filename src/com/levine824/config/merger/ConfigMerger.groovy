package com.levine824.config.merger

import com.levine824.config.merger.strategy.MergeStrategyFactory

class ConfigMerger {
    static final String STRATEGY_MARKER = '_strategy'
    static final String OPTIONS_MARKER = '_options'

    private MergeStrategyFactory factory

    ConfigMerger(MergeStrategyFactory factory) {
        this.factory = factory
    }

    Object merge(Object baseConfig, Object customConfig) {
        def parsed = parse(customConfig)
        def markers = parsed['markers']
        def strategyName = markers[STRATEGY_MARKER] as String
        def options = markers[OPTIONS_MARKER] as Map
        def handler = { be, ce -> merge(be, ce) }
        def strategy = strategyName
                ? factory.create(strategyName, options)
                : factory.create('deep', [handler: handler])
        return strategy?.merge(baseConfig, parsed['config'])
    }

    private static Map parse(Object customConfig) {
        def markers = [:]
        def config = customConfig
        if (customConfig instanceof Map) {
            def copy = customConfig + [:]
            markers[STRATEGY_MARKER] = copy.remove(STRATEGY_MARKER)
            markers[OPTIONS_MARKER] = copy.remove(OPTIONS_MARKER)
            config = copy
        } else if (customConfig instanceof List) {
            def copy = customConfig + []
            def index = copy.findIndexOf {
                it instanceof Map && it.containsKey(STRATEGY_MARKER) &&
                        (it.size() < 2 || (it.size() == 2 && it.containsKey(OPTIONS_MARKER)))
            }
            if (index != -1) {
                markers.putAll(copy[index] as Map)
                copy.remove(index)
            }
            config = copy
        }
        return [markers: markers, config: config]
    }
}
