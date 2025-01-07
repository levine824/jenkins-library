package com.levine824.jenkins.config

import com.levine824.jenkins.utils.MapUtils
import com.levine824.jenkins.utils.StringUtils

class ConfigHelper {
    private Map config

    ConfigHelper(Map config) {
        this.config = config
    }

    Object getGeneralConfig(String key) {
        return getConfig(ConfigType.GENERAL, key)
    }

    Map getStageConfig(String stageName) {
        return getConfig(ConfigType.STAGE, stageName) as Map
    }

    Map getStepConfig(String stepName) {
        return getConfig(ConfigType.STEP, stepName) as Map
    }

    /**
     * Gets the config map and converts all keys to the environment variable.
     * If nested map, this method will flatten this map.
     *
     * @param type the config type, representing the top node of yaml
     * @param key the config name
     * @return the {@code Map}
     */
    Map getConfigAsEnv(ConfigType type, String key) {
        def value = getConfig(type, key)
        return MapUtils.toEnv(MapUtils.flatten([(key): value]))
    }

    /**
     * Iterates through a {@code Set}, converting the config to the environment variable.
     *
     * @param type the config type, representing the top node of yaml
     * @param configKeys the string composed of separators and nodes
     * @param regex the delimiting regular expression
     * @return the {@code Map}
     */
    Map getConfigAsEnv(ConfigType type, Set<String> configKeys, String regex = '_') {
        def map = [:]
        configKeys.each { configKey ->
            def key = StringUtils.toEnv(configKey)
            def value = getConfig(type, StringUtils.toStringArray(configKey, regex))
            map.put(key, value)
        }
        return MapUtils.toEnv(MapUtils.flatten(map))
    }

    private Object getConfig(ConfigType type, String... keys) {
        return getConfig(type.toString(), keys)
    }

    private Object getConfig(String type, String... keys) {
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