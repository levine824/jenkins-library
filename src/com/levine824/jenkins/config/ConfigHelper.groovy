package com.levine824.jenkins.config

import com.levine824.jenkins.utils.MapUtils
import com.levine824.jenkins.utils.StringUtils

class ConfigHelper {
    private static final String SEPARATOR = '_'

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

    Map getConfigAsEnv(ConfigType type, String key) {
        def value = getConfig(type, key)
        return MapUtils.toEnv(MapUtils.flatten([(key): value]))
    }

    Map getConfigAsEnv(ConfigType type, Set<String> configKeys) {
        def map = [:]
        configKeys.each { configKey ->
            def key = StringUtils.toEnv(configKey)
            def value = getConfig(type, StringUtils.toStringArray(configKey, SEPARATOR))
            map.put(key, value)
        }
        return MapUtils.flatten(map)
    }

    private def getConfig(ConfigType type, String... keys) {
        return getConfig(type.toString(), keys)
    }

    private def getConfig(String type, String... keys) {
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