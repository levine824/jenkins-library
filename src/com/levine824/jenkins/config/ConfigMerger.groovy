package com.levine824.jenkins.config

class ConfigMerger {

    private MergeOptions options

    ConfigMerger(MergeOptions options = new MergeOptions()) {
        this.options = options
    }

    Object merge(Object base, Object custom) {
        return merge(base, custom, null)
    }

    Object merge(Object base, Object custom, MergeStrategy parentStrategy) {
        String strategy = extractStrategy(custom)
        strategy = strategy ?: parentStrategy ?: options.defaultStrategy
        return options.strategies[strategy]?.merge(base, custom)
    }


    private String extractStrategy(Object custom) {
        String strategy = null
        if (custom instanceof Map) {
            strategy = custom[options.uniqueKey]
            custom.remove(options.uniqueKey)
        } else if (custom instanceof List) {
            int index = custom.findLastIndexOf { element ->
                element instanceof Map && element.containsKey(options.uniqueKey)
            }
            if (index > 0) {
                strategy = custom[index][options.uniqueKey]
                custom.remove(index)
            }
        }
        return strategy
    }
}
