import com.levine824.jenkins.config.ConfigHelper
import com.levine824.jenkins.config.ConfigLoader
import com.levine824.jenkins.context.PipelineContext
import com.levine824.jenkins.utils.MapUtils
import com.levine824.jenkins.utils.StringUtils
import groovy.transform.Field

@Field String STEP_NAME = 'mainTest'
@Field Set GENERAL_CONFIG_KEYS = [
        'projectName',
        'url'
]
@Field Set STAGE_CONFIG_KEYS = [
        'build.maven.command'
]

def main() {
    String config1 = '''
general:
  projectName: 'jenkins-library'
  url:
    - baidu
    - google
stage:
  build:
    maven:
      command: 'mvn clean package'
step:
  mainTest:
    result: success
'''
    String config2 = '''
general:
  url:
    - github
'''
    //def ctx = new PipelineContext(this, ConfigLoader.load(config1, config2))
    //println(ctx.getEnv(this))
    println(StringUtils.toEnvCase('build.maven.command'))
}

main()