package com.levine824.jenkins.context

import com.levine824.jenkins.config.ConfigLoader
import com.levine824.jenkins.config.ConfigType

class PipelineContext {
    private Script script
    private ConfigLoader loader

    private static volatile PipelineContext instance

    static PipelineContext getInstance(Script script, String yaml) {
        if (instance == null) {
            synchronized (PipelineContext.class) {
                if (instance == null) {
                    instance = new PipelineContext(script, yaml)
                }
            }
        }
        return instance
    }

    private PipelineContext(Script script, String yaml) {
        this.script = script
        this.loader = ConfigLoader.load(yaml)
    }

    Map getEnvironment(Script step, ConfigType type, String name) {
        return [:]
    }

}