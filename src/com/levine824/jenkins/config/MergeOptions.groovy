package com.levine824.jenkins.config

class MergeOptions {
    List<String> identifierKeys = ['id', 'name']
    MergeStrategy defaultMergeStrategy = MergeStrategy.DEEP_MERGE
}
