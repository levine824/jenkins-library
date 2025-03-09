package com.levine824.jenkins.config

import com.levine824.jenkins.utils.MapUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

class ConfigHelper implements Serializable {
    private static final Pattern CONFIG_PATHS_PATTERN = ~/(\w+)_CONFIG_PATHS$/

    private Map config
    private Script step
    private Map fields

    ConfigHelper(Map config, Script step) {
        this.config = config
        this.step = step
        this.fields = getStepFields()
    }

    @Override
    Object getProperty(String property) {
        try {
            return super.getProperty(property)
        } catch (MissingPropertyException mpe) {
            return fields.get(property)
        }
    }

    Map parse() {
        Map merged = getStepConfig()
        fields.each { key, value ->
            Matcher matcher = key =~ CONFIG_PATHS_PATTERN
            if (matcher.matches()) {
                String rawType = matcher.group(1)
                String type = rawType.toLowerCase()
                String name = stepFields["${rawType}_NAME"] ?: step.env."${rawType}_NAME" ?: null
                merged.putAll(getConfigByPaths(type, name, value as Set))
            }
        }
        return merged
    }

    Map getConfigByPaths(String type, String name, Set paths) {
        Map merged = [:]
        Map config = getConfig(type, name) as Map
        paths.each { path ->
            merged.put(path, MapUtils.getByPath(config, path.toString()))
        }
        return merged
    }

    Object getConfig(String type = '', String name = '') {
        return type
                ? MapUtils.get(config, [type, name].findAll() as String[])
                : config
    }

    private Map getStepFields() {
        Map fields = [:]
        step.getClass().getDeclaredFields().each { field ->
            if (!field.synthetic && field.declaringClass == step.class) {
                field.setAccessible(true)
                fields.put(field.name, field.get(step))
            }
        }
        return fields
    }

    private Map getStepConfig() {
        String stepName = fields['STEP_NAME']?.toString()
        if (!stepName) {
            throw new IllegalArgumentException('Step has no public name property!')
        }
        return getConfig('step', stepName) as Map
    }
}
