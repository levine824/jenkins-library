import com.levine824.jenkins.PipelineContext
import com.levine824.jenkins.config.ConfigLoader
import groovy.transform.Field

@Field String STEP_NAME = getClass().getName()

def call(Map args) {
    def defaultYaml = libraryResource 'config.yml'
    def loader = new ConfigLoader().load(defaultYaml)
    if (args.configFile) {
        def customYaml = readFile args.configFile
        loader.load(customYaml)
    }
    env.PIPELINE_CONTEXT = new PipelineContext(args.script as Script, loader.config)
}
