package com.levine824.jenkins.config

class ConfigHelper implements Serializable {
    private Script step
    private String name
    private Set parameters
    private ConfigLoader loader

    ConfigHelper(Script step, ConfigLoader loader) {
        this.step = step
        this.loader = loader
        this.name = step.STEP_NAME
        this.parameters = step.PARAMETER_KEYS
        if (!this.name) throw new IllegalArgumentException('Step has no public name property!')
    }


}
