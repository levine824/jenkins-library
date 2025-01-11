package com.levine824.jenkins.context

import com.levine824.jenkins.config.ConfigHelper
import com.levine824.jenkins.config.ConfigType
import com.levine824.jenkins.utils.MapUtils
import com.levine824.jenkins.utils.StringUtils

class PipelineContext implements Serializable {
    private Script script

    private Map config

    PipelineContext(Script script, Map config) {
        this.script = script
        this.config = config
    }

    Map getEnv(Script step, Map args = [:]) {
        String stepName = getStepProperty(step, 'STEP_NAME', null)
        Set<String> parameterKeys = getStepProperty(step, 'PARAMETER_KEYS', null) as Set
        Set<String> generalConfigKeys = getStepProperty(step, 'GENERAL_CONFIG_KEYS', null) as Set
        Set<String> stageConfigKeys = getStepProperty(step, 'STAGE_CONFIG_KEYS', null) as Set

        Map parameters = script.params.each {}
        Map stepConfig = ConfigHelper.getConfig(config, ConfigType.STEP, stepName) as Map
        Map generalConfig = ConfigHelper.getConfig(config, ConfigType.GENERAL, generalConfigKeys, '.')
        // stageName not sure
        Map stageConfig = ConfigHelper.getConfig(config, ConfigType.STAGE, stageConfigKeys, '.')

        Map stepEnv = MapUtils.toEnvCase(stepConfig)
        Map generalEnv = MapUtils.toEnvCase(generalConfig)
        Map stageEnv = MapUtils.toEnvCase(stageConfig)
        Map argsEnv = MapUtils.toEnvCase(args)

        stepEnv.putAll(parameters)
        stepEnv.putAll(generalEnv)
        stepEnv.putAll(stageEnv)
        stepEnv.putAll(argsEnv)
        return stepEnv
    }

    private static Object getStepProperty(Script step, String name, Object defaultValue) {
        return step.metaClass.hasProperty(step, name) ? step.getProperty(name) : defaultValue
    }

}
