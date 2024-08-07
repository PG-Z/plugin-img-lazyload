package top.aiheiyo.img.lazyload;

import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * Description: 启动
 *
 * @author : evan  Date: 2024/8/7
 */
@Component
public class ImgLazyLoadPlugin extends BasePlugin {

    private final SchemeManager schemeManager;
  
    public ImgLazyLoadPlugin(PluginContext context, SchemeManager schemeManager) {
        super(context);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        System.out.println("插件启动成功！");
    }

    @Override
    public void stop() {
        System.out.println("插件停止！");
    }
}
