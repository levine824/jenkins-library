package com.levine824.jenkins.config

/*
 * ConfigType represents the top node of config yaml.
 */

enum ConfigType {
    GENERAL, STAGE, STEP, POST

    @Override
    String toString() {
        return this.name().toLowerCase()
    }

    static ConfigType fromString(String text) {
        for (ConfigType type : values()) {
            if (type.toString() == text) {
                return type
            }
        }
        return null
    }
}