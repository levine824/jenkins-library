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
        'maven.command'
]

def main() {
    // 假设我们有以下配置Map
    def configMap = [
            'userName'  : 'admin',
            'password'  : 'secret',
            'serverPort': 8080
    ]

// 转换键的Closure，例如将驼峰式转为短横线分隔形式
    def convertKeyClosure = { key ->
        key.toString().replaceAll(/([a-z])([A-Z])/, /$1-$2/).toLowerCase()
    }

// 应用Closure到每个键上
    def convertedMap = configMap.collectEntries { key, value ->
        [(convertKeyClosure(key)): value]
    }

// 打印转换后的Map
    println convertedMap
}

main()