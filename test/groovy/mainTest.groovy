import com.levine824.jenkins.config.ConfigLoader

def main() {
    def config = ConfigLoader.load(new FileInputStream('config.yml'))
    println(config)
}

main()
