package com.levine824.jenkins.config

class ConfigHelper {
    private Map config

    ConfigHelper(Map config) {
        this.config = config
    }

    Object getGeneralConfig(String name) {
        return get(ConfigType.GENERAL, name)
    }

    Map getStageConfig(String stageName) {
        return get(ConfigType.STAGE, stageName) as Map
    }

    Map getStepConfig(String stepName) {
        return get(ConfigType.STEP, stepName) as Map
    }

    private def get(ConfigType type, String... keys) {
        return get(type.toString(), keys)
    }

    private def get(String type, String... keys) {
        try {
            return keys.inject(this.config?.get(type) ?: [:]) { value, key ->
                if (value instanceof Map) {
                    value = value?.get(key) ?: [:]
                } else {
                    throw new IllegalArgumentException("The key called " + key + " is not existed.")
                }
                return value
            }
        } catch (MissingPropertyException ignored) {
            return [:]
        }
    }

}
