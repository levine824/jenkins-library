package com.levine824.jenkins.context

import com.levine824.jenkins.config.ConfigType

class StepContext {
    private Script step
    private Map args
    // property must have
    private String stepName
    // property may have
    private Set parameterKeys
    private Map<String, Set> properties = [:]

    StepContext(Script step, Map args = [:]) {
        this.step = step
        this.args = args
        getStepProperties()
    }

    private getStepProperties() {
        this.stepName = getStepProperty('STEP_NAME')
        if (!stepName) throw new IllegalArgumentException('Step has no name property!')
        this.parameterKeys = getStepProperty('PARAMETER_KEYS') as Set
        for (ConfigType type : ConfigType.values()) {
            String key = type.name() + '_CONFIG_KEYS'
            Set value = getStepProperty(key) as Set
            if (value) {
                properties.put(key, value)
            }
        }
    }

    private Object getStepProperty(String property) {
        return step.getMetaClass().hasProperty(step, property) ? step.getProperty(property) : null
    }

}