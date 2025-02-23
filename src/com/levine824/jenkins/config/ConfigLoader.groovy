package com.levine824.jenkins.config

import org.yaml.snakeyaml.Yaml

class ConfigLoader {
    private Map config = [:]

    ConfigLoader load(String yaml) {
        Map customConfig
        try {
            customConfig = new Yaml().load(yaml)
        } catch (Exception e) {
            throw new Exception("Failed to load configuration", e)
        }
        ConfigMerger merger = new ConfigMerger()
        merger.merge(config, customConfig)
        return this
    }
}
