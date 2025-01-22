package com.levine824.jenkins.context

import com.levine824.jenkins.config.ConfigHelper
import com.levine824.jenkins.config.ConfigLoader
import com.levine824.jenkins.utils.MapUtils

class PipelineContext implements Serializable {
    private Script script
    private ConfigLoader loader

    PipelineContext(Script script, ConfigLoader loader) {
        this.script = script
        this.loader = loader
    }

    Map getEnv(Script step) {
        ConfigHelper helper = new ConfigHelper(loader, step)
        Map config = helper.getAllConfig()
        return MapUtils.toEnvCase(MapUtils.flatten(config, '', '.'))
    }

}
