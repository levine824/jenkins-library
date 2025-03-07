package com.levine824.jenkins

import com.levine824.jenkins.config.ConfigHelper
import com.levine824.jenkins.utils.EnvironmentUtils
import com.levine824.jenkins.utils.MapUtils

class PipelineContext implements Serializable {
    private Script script
    private Map config

    PipelineContext(Script script, Map config) {
        this.script = script
        this.config = config
    }

    List getEnv(Script step) {
        ConfigHelper helper = new ConfigHelper(config, step)
        return EnvironmentUtils.toEnvVars(MapUtils.flatten(helper.parse()))
    }
}
