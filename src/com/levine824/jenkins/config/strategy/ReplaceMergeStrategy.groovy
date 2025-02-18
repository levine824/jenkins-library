package com.levine824.jenkins.config.strategy

import com.levine824.jenkins.config.merger.MergeStrategy

class ReplaceMergeStrategy implements MergeStrategy {

    @Override
    Object merge(Object base, Object custom) {
        return custom
    }

    @Override
    String getName() {
        return "replace"
    }
}