package com.levine824.jenkins.config

import com.levine824.jenkins.utils.MapUtils
import org.yaml.snakeyaml.Yaml

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ConfigLoader {
    private Map config = [:]

    // 加载默认配置文件
    ConfigLoader loadDefault() {
        Path path = Paths.get(getClass().getClassLoader().getResource("config.yml").toURI())
        loadConfig(path)
        return this
    }

    // 加载单个自定义配置文件
    ConfigLoader loadCustom(String filePath) {
        Path path = Paths.get(filePath)
        loadConfig(path)
        return this
    }

    // 加载多个自定义配置文件
    ConfigLoader loadCustom(List<String> filePaths) {
        filePaths.each { filePath ->
            loadCustom(filePath)
        }
        return this
    }

    private void loadConfig(Path path) {
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Config file not found: ${path}")
        }
        try (InputStream is = Files.newInputStream(path)) {
            Yaml yaml = new Yaml()
            Map map = new Yaml().load(is)
            config = MapUtils.mergeMaps(config, map)
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config file: ${path}", e)
        }
    }

    // 获取配置
    Map getConfig() {
        return config
    }

}