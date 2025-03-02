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
        this.fields = getFields()
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
        Map merged = stepConfig()
        fields.each { key, value ->
            Matcher matcher = key =~ CONFIG_PATHS_PATTERN
            if (matcher.matches()) {
                String raw = matcher.group(1)
                String type = raw.toLowerCase()
                String name = fields["${raw}_NAME"] ?: step.env."${raw}_NAME"
                merged.putAll(getConfigByPath(type, name, value as Set))
            }
        }
        return merged
    }

    Map getConfigByPath(String type, String name, Set<String> paths) {
        Map merged = [:]
        Map raw = getConfig(type, name) as Map
        paths.each {
            merged.put(it, MapUtils.getByPath(raw, it))
        }
        return merged
    }

    Object getConfig(String type = '', String name = '') {
        return type
                ? MapUtils.getByKeys(config, [type, name].findAll() as String[])
                : config
    }

    private Map getFields() {
        Map fields = [:]
        step.getClass().getDeclaredFields().each {
            if (!it.synthetic && it.declaringClass == step.class) {
                it.setAccessible(true)
                fields.put(it.name, it.get(step))
            }
        }
        return fields
    }

    private Map stepConfig() {
        String stepName = fields['STEP_NAME']?.toString()
        if (!stepName) {
            throw new IllegalArgumentException('Step has no public name property!')
        }
        return getConfig('step', stepName) as Map
    }
}
