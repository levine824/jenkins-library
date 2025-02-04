package com.levine824.jenkins.config

import com.levine824.jenkins.utils.MapUtils
import groovy.transform.Field

import java.util.regex.Matcher
import java.util.regex.Pattern

class ConfigHelper {
    private Map config
    private Script step
    private Map properties

    ConfigHelper(Map config, Script step) {
        this.config = config
        this.step = step
        step.getClass().declaredFields.each { field ->
            if (field.getAnnotation(Field)) {
                properties.put(field.name, field.get(this))
            }
        }
    }

    @Override
    Object getProperty(String property) {
        try {
            return super.getProperty(property)
        } catch (MissingPropertyException mpe) {
            return properties.get(property)
        }
    }

    @Override
    Object invokeMethod(String name, Object args) {
        try {
            return super.invokeMethod(name, args)
        } catch (MissingMethodException mme) {
            String regex = "get(.*)Config"
            Matcher matcher = Pattern.compile(regex).matcher(name)
            if (matcher.matches()) {
                return getConfig(matcher.group(1).toLowerCase(), (String) args)
            } else {
                throw mme
            }
        }
    }

    Map getStepConfig() {
        def map = (Map) getConfigByType('step', (String) getProperty('STEP_NAME'))
        properties.keySet().each { str ->
            map.putAll(getConfig(str.replaceAll(STEP_PROPERTY_SUFFIX, "").toLowerCase()))
        }
        return map
    }

    private Map getConfig(String type, String name = "") {
        return getConfigByPath(type, name, (Set) getProperty(type.toUpperCase() + '_CONFIG_KEYS'))
    }

    private Map getConfigByPath(String type, String name = "", Set<String> configKeys) {
        Map typedConfig = (Map) getConfigByType(type, name)
        def map = [:]
        configKeys.each { configKey ->
            map.put(configKey, MapUtils.getNestedValue(typedConfig, configKey))
        }
        return map
    }

    private Object getConfigByType(String type, String name = "") {
        def typedConfig = config?.get(type)
        if (!name) return typedConfig
        return (typedConfig instanceof Map) ? typedConfig.get(name) : typedConfig
    }
}