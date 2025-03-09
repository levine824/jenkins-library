package com.levine824.jenkins.config

import org.yaml.snakeyaml.Yaml

class ConfigLoader {
    private ConfigMerger merger
    private Map config = [:]

    ConfigLoader(ConfigMerger merger = new DefaultConfigMerger()) {
        this.merger = merger
    }

    ConfigLoader load(String yaml) {
        try {
            Yaml parser = new Yaml()
            Map custom = parser.load(yaml)
            config = merger.merge(config, custom)
            return this
        } catch (Exception e) {
            throw new Exception("Failed to load YAML: ${e.message}", e)
        }
    }

    Map getConfig() {
        return config
    }
}
