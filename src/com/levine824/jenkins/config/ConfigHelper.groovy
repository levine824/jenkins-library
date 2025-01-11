package com.levine824.jenkins.config

import com.levine824.jenkins.utils.MapUtils

class ConfigHelper {
    private Script step

    private String stepName

    private Object getStepProperty(String property) {
        return step.getMetaClass().hasProperty(step, property) ? step.getProperty(property) : null
    }

    static Map configToEnv(Map config) {
        return MapUtils.toEnvCase(MapUtils.flatten(config))
    }

    static Map getConfig(Map config, ConfigType type, Set<String> configKeys, String regex) {
        Map map = [:]
        configKeys.each { configKey ->
            Object value = getConfig(config, type, configKey, regex)
            map.put(configKey, value)
        }
        return map
    }

    static Object getConfig(Map config, ConfigType type, String configKey, String regex) {
        return getConfig(config, type, configKey.split(regex))
    }

    static Object getConfig(Map config, ConfigType type, String... keys) {
        return getConfig(config, type.toString(), keys)
    }

    static Object getConfig(Map config, String type, String... keys) {
        try {
            return keys.inject(config?.get(type) ?: [:]) { value, key ->
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