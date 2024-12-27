package com.levine824.jenkins.config

import org.yaml.snakeyaml.Yaml

class ConfigLoader {

    private Map config

    static ConfigLoader load(String yaml) {
        try {
            Map config = new Yaml().load(yaml)
            return new ConfigLoader(config)
        } catch (Exception e) {
            throw new Exception("Failed to load configuration:" + e.getMessage())
        }
    }

    private ConfigLoader(Map config) {
        this.config = config
    }

    def generalConfig(String name) {
        return getConfig(ConfigType.GENERAL, name)
    }

    Map stageConfig(String stageName) {
        return getConfig(ConfigType.STAGE, stageName) as Map
    }

    Map stepConfig(String stepName) {
        return getConfig(ConfigType.STEP, stepName) as Map
    }

    private def getConfig(ConfigType type, String... names) {
        return getConfig(type.toString(), names)
    }

    private def getConfig(String type, String... names) {
        try {
            def config = this.config?.get(type) ?: [:]
            names.each { name ->
                if (config instanceof Map) {
                    config = config?.get(name) ?: [:]
                } else {
                    throw new IllegalArgumentException("The key called " + name + " is not existed.")
                }
            }
            return config
        } catch (MissingPropertyException ignored) {
            return [:]
        }
    }

}