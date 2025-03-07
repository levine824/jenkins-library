package com.levine824.jenkins.config

import org.yaml.snakeyaml.Yaml

class ConfigLoader {
    private ConfigMerger merger

    ConfigLoader(ConfigMerger merger = new DefaultConfigMerger()) {
        this.merger = merger
    }

    Map load(List<String> contents) {
        Map config = [:]
        contents?.each { content ->
            config = merger.merge(config, load(content)) as Map
        }
        return config
    }

    static Map load(String content) {
        try {
            Yaml parser = new Yaml()
            return parser.load(content)
        } catch (Exception e) {
            throw new Exception("Failed to load YAML: ${e.message}", e)
        }
    }
}
