package com.levine824.jenkins.config

import org.yaml.snakeyaml.Yaml

class ConfigLoader {
    private Map config = [:]
    private ConfigMerger merger

    ConfigLoader(ConfigMerger merger = new DefaultConfigMerger()) {
        this.merger = merger
    }

    Map load(List<String> contents) {
        Yaml parser = new Yaml()
        contents.each { load(it, parser) }
        return config
    }

    Map load(String content, Yaml parser = new Yaml()) {
        Map customConfig
        try {
            customConfig = parser.load(content)
        } catch (Exception e) {
            throw new Exception("Failed to load configuration", e)
        }
        config = merger.merge(config, customConfig) as Map
        return config
    }
}
