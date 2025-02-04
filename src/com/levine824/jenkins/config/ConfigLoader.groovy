package com.levine824.jenkins.config

import org.yaml.snakeyaml.Yaml

class ConfigLoader {
    private Map config

    ConfigLoader(Map initConfig = [:]) {
        this.config = initConfig ?: [:]
    }

    ConfigLoader load(String yaml, MergeOptions options) {
        Map customConfig
        try {
            customConfig = new Yaml().load(yaml)
        } catch (Exception e) {
            throw new Exception("Failed to load configuration", e)
        }
        ConfigMerger.merge(config, customConfig, options)
        return this
    }

    ConfigLoader load(List<String> yamls, MergeOptions options) {
        yamls.each { yaml -> load(yaml, options) }
        return this
    }

    Map getConfig() {
        return config
    }
}