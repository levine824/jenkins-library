package com.levine824.jenkins.config

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import groovy.transform.stc.SecondParam

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

    private def getConfig(ConfigType type, Set<String> configKeys,
                          @ClosureParams(SecondParam.FirstGenericType.class) Closure<String[]> closure) {
        def map = [:]
        configKeys.each { configKey ->
            def keys = closure.call(configKey)
            def value = getConfig(type, keys)
            map.put(configKey, value)
        }
        return map
    }

    private def getConfig(ConfigType type, String configKey,
                          @ClosureParams(SecondParam.class) Closure<String[]> closure) {
        def keys = closure.call(configKey)
        return getConfig(type, keys)
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