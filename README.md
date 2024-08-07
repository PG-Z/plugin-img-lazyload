# PluginImgLazyLoad
>适配Halo插件: 支持图片懒加载。

<p align="center">
    <a href="/">
        <img src="https://img.shields.io/github/v/release/PG-Z/plugin-img-lazyload?color=F38181&amp;label=version&amp;logo=v&amp;logoColor=F38181&amp;style=for-the-badge" referrerpolicy="no-referrer" alt="plugin version" />
    </a>
    <a href="/">
        <img src="https://img.shields.io/github/downloads/PG-Z/plugin-img-lazyload/total?color=FCE38A&amp;logo=github&amp;logoColor=FCE38A&amp;style=for-the-badge" referrerpolicy="no-referrer" alt="github downloads" />
    </a>
    <a href="/">
        <img src="https://img.shields.io/github/release-date/PG-Z/plugin-img-lazyload?color=95E1D3&amp;label=release date&amp;logo=puppet&amp;logoColor=95E1D3&amp;style=for-the-badge" referrerpolicy="no-referrer" alt="release-date" />
    </a>
    <img src="https://img.shields.io/github/last-commit/PG-Z/plugin-img-lazyload?style=for-the-badge&amp;logo=lospec&amp;logoColor=a6d189" referrerpolicy="no-referrer" alt="last-commit" />
    <a href="/"><img src="https://img.shields.io/badge/halo-%3E=2.17.0-8caaee?style=for-the-badge&amp;logo=hexo&amp;logoColor=8caaee" referrerpolicy="no-referrer" alt="Required Halo version" /></a>
</p>

## 开发环境

插件开发的详细文档请查阅：<https://docs.halo.run/developer-guide/plugin/introduction>

所需环境：

1. Java 17
2. Node 20
3. pnpm 9
4. Docker (可选)

克隆项目：

```bash
git clone git@github.com:halo-sigs/plugin-starter.git

# 或者当你 fork 之后

git clone git@github.com:{your_github_id}/plugin-starter.git
```

```bash
cd path/to/plugin-starter
```

### 运行方式 1（推荐）

> 此方式需要本地安装 Docker

```bash
# macOS / Linux
./gradlew pnpmInstall

# Windows
./gradlew.bat pnpmInstall
```

```bash
# macOS / Linux
./gradlew haloServer

# Windows
./gradlew.bat haloServer
```

执行此命令后，会自动创建一个 Halo 的 Docker 容器并加载当前的插件，更多文档可查阅：<https://docs.halo.run/developer-guide/plugin/basics/devtools>

### 运行方式 2

> 此方式需要使用源码运行 Halo

编译插件：

```bash
# macOS / Linux
./gradlew build

# Windows
./gradlew.bat build
```

修改 Halo 配置文件：

```yaml
halo:
  plugin:
    runtime-mode: development
    fixedPluginPath:
      - "/path/to/plugin-starter"
```

最后重启 Halo 项目即可。

## lazyload js

[Github](https://github.com/tuupola/lazyload)
