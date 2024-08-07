package top.aiheiyo.img.lazyload;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.PluginContext;
import run.halo.app.theme.dialect.TemplateHeadProcessor;
import java.util.Properties;

/**
 * Description: Processor
 *
 * @author : evan  Date: 2024/8/7
 */
@Component
@RequiredArgsConstructor
public class ImgLazyLoadProcessor implements TemplateHeadProcessor {

    static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}");

    private final PluginContext pluginContext;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
                              IElementModelStructureHandler structureHandler) {
        final IModelFactory modelFactory = context.getModelFactory();
        model.add(modelFactory.createText(mapMarkerScript()));
        return Mono.empty();
    }

    private String mapMarkerScript() {

        final Properties properties = new Properties();
        properties.setProperty("version", pluginContext.getVersion());

        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders("""
                <!-- PluginImgLazyLoad start -->
                <script src="/plugins/PluginImgLazyLoad/assets/static/js/lazyload.min.js?version=${version}"></script>
                <script src="/plugins/PluginImgLazyLoad/assets/static/js/scrollstop.min.js?version=${version}"></script>
                <script src="/plugins/PluginImgLazyLoad/assets/static/js/img-lazyload.js?version=${version}"></script>
                <!-- PluginImgLazyLoad end -->
                """, properties);
    }
}