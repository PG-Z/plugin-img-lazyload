package top.aiheiyo.img.lazyload.core;

import lombok.Data;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;

/**
 * Description: 插件配置
 *
 * @author : evan  Date: 2024/8/7
 */
public class Settings {

    public static Mono<BasicConfig> getBasicConfig(ReactiveSettingFetcher settingFetcher) {
        return settingFetcher.fetch(BasicConfig.GROUP, BasicConfig.class);
    }

    @Data
    public static class BasicConfig {
        public static final String GROUP = "basic";

        /**
         * 开关
         */
        private Boolean status;

        /**
         * 设置占位图片路径
         */
        private String loadImgUrl;
        /**
         * 忽略属性
         */
        private String ignoreAttr;
    }

}
