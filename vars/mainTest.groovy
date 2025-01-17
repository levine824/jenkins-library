import com.levine824.jenkins.config.ConfigLoader
import com.levine824.jenkins.context.PipelineContext
import groovy.transform.Field

@Field String STEP_NAME = 'mainTest'
@Field Set PARAMETER_KEYS = [
        'COMMIT_ID'
]
@Field Set GENERAL_CONFIG_KEYS = [
        'projectName',
        'git.url'
]
@Field Set STAGE_CONFIG_KEYS = [
        'build.maven.command'
]

def main(Map args) {
    def yaml = args.configFile ? (readFile ${args.configFile}) : (libraryResource 'config.yml')
    def ctx = new PipelineContext(args.script as Script, ConfigLoader.load(yaml))
    println(ctx.getEnv(this))
}

main()