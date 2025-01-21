package com.levine824.jenkins.config

import com.levine824.jenkins.utils.MapUtils
import org.yaml.snakeyaml.Yaml

import java.util.regex.Pattern

class ConfigLoader {
    private Map config

    static ConfigLoader load(String... yaml) {
        try {
            Map config = [:]
            yaml.each { config = MapUtils.merge(config, new Yaml().load(it)) }
            return new ConfigLoader(config)
        } catch (Exception e) {
            throw new Exception("Failed to load configuration:" + e.getMessage())
        }
    }

    private ConfigLoader(Map config) {
        this.config = config
    }

    Map get(Set<String> combinedKeys, String delimiter = '.') {
        Map map = [:]
        combinedKeys.each { combinedKey ->
            Object value = get(combinedKey.split(Pattern.quote(delimiter)))
            map.put(combinedKey, value)
        }
        return map
    }

    Object get(String... keys) {
        return keys.inject(config) { value, key ->
            return value instanceof Map ? value.get(key) : null
        }
    }

}