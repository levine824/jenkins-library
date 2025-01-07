package com.levine824.jenkins.config

import org.yaml.snakeyaml.Yaml

/**
 * This class provides static methods to load the configuration.
 */
class ConfigLoader {

    /**
     * Parse the only YAML document in a String and produce the Map.
     *
     * @param yaml YAML data to load from
     * @return parsed {@code Map}
     */
    static Map load(String yaml) {
        try {
            return new Yaml().load(yaml)
        } catch (Exception e) {
            throw new Exception("Failed to load configuration:" + e.getMessage())
        }
    }

}