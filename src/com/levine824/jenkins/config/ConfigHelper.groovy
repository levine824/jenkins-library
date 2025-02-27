package com.levine824.jenkins.config

import com.levine824.jenkins.utils.MapUtils
import groovy.transform.Field

import java.util.regex.Matcher
import java.util.regex.Pattern

class ConfigHelper implements Serializable {
    private Map config
    private Script step
    private Map fields

    ConfigHelper(Map config, Script step) {
        this.config = config
        this.step = step
        this.fields = getFields(step)
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
        Pattern pattern = ~/(\w+)_CONFIG_PATHS/
        Map merged = stepConfig()
        fields.keySet().each {
            Matcher matcher = it =~ pattern
            if (matcher.matches()) {
                String type = matcher.group(1).replaceAll('_CONFIG_PATHS', '').toLowerCase()
                Set<String> paths = fields[type] as Set
                merged.putAll("${type}Config"(paths) as Map)
            }
        }
        return merged
    }

    Object generalConfig(Set<String> paths = []) {
        getConfig('general', '', paths)
    }

    Map stageConfig(Set<String> paths = []) {
        getConfig('stage', step.env.STAGE_NAME as String, paths) as Map
    }

    Map stepConfig(Set<String> paths = []) {
        getConfig('step', fields.get('STEP_NAME') as String, paths) as Map
    }

    Object postConfig(Set<String> paths = []) {
        getConfig('post', '', paths)
    }

    Object getConfig(String type = '', String name = '', Set<String> paths = []) {
        def raw = type
                ? MapUtils.getByKeys(config, [type, name].findAll() as String[])
                : config
        if (!paths) return raw
        raw instanceof Map
                ? paths.collectEntries { [it, MapUtils.getByPath(raw, it)] }
                : [:]
    }

    private static Map getFields(Script step) {
        Map fields = [:]
        step.getClass().declaredFields.each {
            if (it.getAnnotation(Field)) fields.put(it.name, it.get(this))
        }
        return fields
    }
}
