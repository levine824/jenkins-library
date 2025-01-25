package com.levine824.jenkins.config

/**
 * ConfigType represents the top node of config yaml.
 */
enum ConfigType {
    GENERAL, STAGE, STEP, POST

    @Override
    String toString() {
        return this.name().toLowerCase()
    }

}