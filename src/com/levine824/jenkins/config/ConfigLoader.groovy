package com.levine824.jenkins.config

import com.levine824.jenkins.utils.MapUtils
import org.yaml.snakeyaml.Yaml

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

    Map get(ConfigType type, Set<String> keys, String delimiter = '.') {
        return get(type.toString(), keys, delimiter)
    }

    Map get(String type, Set<String> keys, String delimiter = '.') {
        return MapUtils.get((Map) get(type), keys, delimiter)
    }

    Object get(ConfigType type, String... keys) {
        return MapUtils.get((Map) config.get(type.toString()), keys)
    }

    Object get(String... keys) {
        return MapUtils.get(config, keys)
    }

}