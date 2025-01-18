package com.levine824.jenkins.context

import com.levine824.jenkins.config.ConfigHelper
import com.levine824.jenkins.utils.MapUtils

class PipelineContext implements Serializable {
    private Script script
    private Map config

    private volatile static PipelineContext ctx

    static PipelineContext getInstance(Script script, Map config) {
        if (ctx == null) {
            synchronized (PipelineContext.class) {
                if (ctx == null) {
                    ctx = new PipelineContext(script, config)
                }
            }
        }
        return ctx
    }

    private PipelineContext(Script script, Map config) {
        this.script = script
        this.config = config
    }

    Map getEnv(Script step) {
        ConfigHelper helper = new ConfigHelper(this.config, step)
        Map stepConfig = helper.getStepConfig()
        Map parameters = ConfigHelper.getConfig(script.params as Map, step.PARAMETERS_KEYS as Set, '_')
        return MapUtils.toEnvCase(MapUtils.flatten(stepConfig)).putAll(parameters)
    }

}
