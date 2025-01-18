import com.levine824.jenkins.config.ConfigLoader
import com.levine824.jenkins.context.PipelineContext
import com.levine824.jenkins.utils.MapUtils
import groovy.transform.Field

@Field String STEP_NAME = getClass().getName()

def call(Map args = [:]) {
    def defaultYaml = libraryResource 'config.yml'
    def config = ConfigLoader.load(defaultYaml)
    if (args.containsKey('configFile')) {
        def yaml = readFile args.configFile
        config = MapUtils.merge(config, ConfigLoader.load(yaml))
    }

    def ctx = new PipelineContext(args.script, config)
    println(ctx.config)
}
