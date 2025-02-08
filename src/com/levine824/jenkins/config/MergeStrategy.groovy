package com.levine824.jenkins.config

interface MergeStrategy {
    Object merge(Object base, Object custom)

    String getName()
}

class ReplaceStrategy implements MergeStrategy {
    private String name = "replace"

    @Override
    Object merge(Object base, Object custom) {
        return custom
    }

    @Override
    String getName() {
        return name
    }
}

class AppendStrategy implements MergeStrategy {
    private String name = "append"

    @Override
    Object merge(Object base, Object custom) {
        if (base instanceof Map && custom instanceof Map) {
            base.putAll(custom)
        } else if (base instanceof List && custom instanceof List) {
            base + custom
        } else {
            return custom
        }
    }

    @Override
    String getName() {
        return name
    }
}