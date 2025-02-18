package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy

class AppendMergeStrategy implements MergeStrategy {

    @Override
    Object merge(Object base, Object custom) {
        if (base == null) return custom
        if (custom == null) return base
        if (base instanceof Map && custom instanceof Map) {
            return base + custom
        } else if (base instanceof List && custom instanceof List) {
            return base + custom
        }
        if (base.getClass() != custom.getClass()) {
            throw new IllegalArgumentException("Cannot merge different types: " +
                    "${base.getClass().simpleName} and " +
                    "${custom.getClass().simpleName}")
        }
        return custom
    }

    @Override
    String getName() {
        return "append"
    }
}
