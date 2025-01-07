package com.levine824.jenkins.context

import com.levine824.jenkins.config.ConfigHelper

class PipelineContext implements Serializable {
    private Script script

    private ConfigHelper helper

    PipelineContext(Script script, Map config) {
        this.script = script
        this.helper = new ConfigHelper(config)
    }

    Map getEnv(Script step, Map args = [:]) {
        return [:]
    }


    private static Object getStepProperty(Script step, String name, Object defaultValue) {
        return metaClass.hasProperty(step, name) ? step.getProperty(name) : defaultValue
    }

}
