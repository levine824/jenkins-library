package com.levine824.jenkins.context

import com.levine824.jenkins.config.ConfigHelper

class PipelineContext {
    private Script script

    private ConfigHelper helper

    private final Map<String, Map> cache = [:]

    Map getEnvironment(Script step, Map args = [:]) {
        Map stepEnv = null
        Map sharedEnv = null
        synchronized (this.cache) {
            sharedEnv = this.cache.get(step.STEP_NAME)
        }
        if (sharedEnv != null) {
            stepEnv = sharedEnv
        } else {

        }
    }
}