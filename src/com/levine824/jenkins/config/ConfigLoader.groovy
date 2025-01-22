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

    Map getConfig(String name,Set<String> configKeysSet, String delimiter = '.') {
        return MapUtils.get((Map)getConfig(name), configKeysSet, delimiter)
    }

    Object getConfig(String... keys) {
        return MapUtils.get(config, keys)
    }

}