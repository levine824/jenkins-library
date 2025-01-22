import com.levine824.jenkins.config.ConfigLoader
import com.levine824.jenkins.context.PipelineContext
import com.levine824.jenkins.utils.MapUtils
import groovy.transform.Field

@Field String STEP_NAME = getClass().getName()
@Field String STAGE_NAME = 'init'

def call(Script script, String configFile = '') {
    def defaultYaml = libraryResource 'com/levine824/jenkins/config/config.yml'
    def config = ConfigLoader.load(defaultYaml)
    if (!configFile) {
        def yaml = readFile configFile
        config = MapUtils.merge(config, ConfigLoader.load(yaml))
    }
    def ctx = new PipelineContext(script, config)
    env.PIPELINE_CONTEXT = ctx
}
