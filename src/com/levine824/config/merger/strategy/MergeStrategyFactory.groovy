package com.levine824.config.merger.strategy

import java.util.concurrent.ConcurrentHashMap

class MergeStrategyFactory {
    private Map<String, Closure<MergeStrategy>> builders = new ConcurrentHashMap<>()

    MergeStrategyFactory() {
        init()
    }

    void register(String name, Closure<MergeStrategy> builder) {
        builders.put(name, builder)
    }

    MergeStrategy create(String name, Map options = [:]) {
        def builder = builders[name]
        if (!builder) {
            throw new IllegalArgumentException("unknow strategy: ${name}")
        }
        return builder(options)
    }

    void init() {
        register("append") { new AppendMergeStrategy() }
        register("replace") { new ReplaceMergeStrategy() }
        register("unique") { new UniqueMergeStrategy() }
        register("deep") { Map options -> new DeepMergeStrategy(options) }
    }
}
