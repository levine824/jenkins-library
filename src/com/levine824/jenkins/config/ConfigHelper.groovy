package com.levine824.jenkins.config

class ConfigHelper {
    private Map config

    private Script step
    private String stepName

    ConfigHelper(Map config, Script step) {
        this.config = config
        this.step = step
        this.stepName = getStepProperty('STEP_NAME')
        if (!stepName) throw new IllegalArgumentException('Step has no public name!')
    }

    @Override
    Object getProperty(String property) {
        if (property.endsWith('_KEYS')) {
            return getStepProperty(property)
        } else {
            return super.getProperty(property)
        }
    }

    private Object getStepProperty(String property) {
        return step.getMetaClass().hasProperty(step, property) ? step.getProperty(property) : null
    }

    Map getStepConfig() {
        def map = [:]
        def stepConfig = getConfig(config, ConfigType.STEP, stepName) as Map
        for (ConfigType type : ConfigType.values()) {
            def configKeys = type.name() + '_CONFIG_KEYS'
            map.put(configKeys, getConfig(config, type, getProperty(configKeys) as Set, '.'))
        }
        map.putAll(step.getBinding().getVariables())
        return map
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
        return getConfig(getConfig(config, type.toString()) as Map, keys)
    }

    static Object getConfig(Map config, String... keys) {
        try {
            return keys.inject(config) { value, key ->
                if (value instanceof Map) {
                    value = value?.get(key) ?: [:]
                } else {
                    throw new IllegalArgumentException("The key called " + key + " is not existed.")
                }
                return value
            }
        } catch (MissingPropertyException mpe) {
            return [:]
        }
    }

}