import groovy.transform.Field

@Field String STEP_NAME = getClass().getName()
@Field Set GENERAL_CONFIG_PATHS = [
        'git.credentialsId'
]
@Field Set STAGE_CONFIG_PATHS = [
        'agent.docker.label',
        'agent.docker.image'
]

def call(Map args) {
    withEnv(env.PIPELINE_CONTEXT.getEnv(this)) {
        def credentialsId = args.credentialsId ?: "${GIT_CREDENTIALS_ID}"
        checkout scmGit(
                branches: [[name: "${params.GIT_BRANCH}"]],
                extensions: [cleanBeforeCheckout(deleteUntrackedNestedRepositories: true)],
                userRemoteConfigs: [[credentialsId: "${credentialsId}", url: "${params.GIT_URL}"]]
        )
    }
}
