package com.levine824.jenkins.config

class MergeOptions {
    String strategyKey = '@merge'
    MergeStrategy defaultStrategy = MergeStrategy.DEEP_MERGE
    List<String> uniqueKeys = ['id', 'name']
}
