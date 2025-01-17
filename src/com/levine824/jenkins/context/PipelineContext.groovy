package com.levine824.jenkins.context

import com.levine824.jenkins.config.ConfigHelper
import com.levine824.jenkins.config.ConfigLoader
import com.levine824.jenkins.utils.MapUtils

class PipelineContext implements Serializable {
    private Script script
    private Map config

    PipelineContext(Script script, String yaml = '') {
        this.script = script
        try (InputStream io = getClass().getResourceAsStream('config.yml')) {
            Map defaultConfig = ConfigLoader.load(io)
            this.config = yaml ? MapUtils.merge(defaultConfig, ConfigLoader.load(yaml)) : defaultConfig
        } catch (IOException e) {
            throw new Exception("Failed to load default configuration.")
        }
    }

    Map getEnv(Script step) {
        ConfigHelper helper = new ConfigHelper(this.config, step)
        Map stepConfig = helper.getStepConfig()
        Map parameters = ConfigHelper.getConfig(script.params as Map, step.PARAMETERS_KEYS as Set, '_')
        return MapUtils.toEnvCase(MapUtils.flatten(stepConfig)).putAll(parameters)
    }

}
