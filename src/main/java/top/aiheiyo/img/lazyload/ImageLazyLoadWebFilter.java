package top.aiheiyo.img.lazyload;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.security.AdditionalWebFilter;
import top.aiheiyo.img.lazyload.common.constant.BusConstant;
import top.aiheiyo.img.lazyload.core.Settings;

/**
 * Description: 图片异步加载处理
 *
 * @author : evan  Date: 2024/8/7
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageLazyLoadWebFilter implements AdditionalWebFilter {

    private static final String LOG_PREFIX = "[图片异步加载处理]";

    private final ReactiveSettingFetcher settingFetcher;
    private final ServerWebExchangeMatcher pathMatcher = createPathMatcher();

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return pathMatcher.matches(exchange)
            .flatMap(matchResult -> {
                if (matchResult.isMatch() && shouldOptimize(exchange)) {
                    var decoratedExchange = exchange.mutate()
                        .response(new ImageLazyLodResponseDecorator(exchange))
                        .build();
                    return chain.filter(decoratedExchange);
                }
                return chain.filter(exchange);
            });
    }

    boolean shouldOptimize(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        var statusCode = response.getStatusCode();
        return statusCode != null && statusCode.isSameCodeAs(HttpStatus.OK);
    }

    ServerWebExchangeMatcher createPathMatcher() {
        var pathMatcher = pathMatchers(HttpMethod.GET, "/**");
        var mediaTypeMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        mediaTypeMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        return new AndServerWebExchangeMatcher(pathMatcher, mediaTypeMatcher);
    }

    class ImageLazyLodResponseDecorator extends ServerHttpResponseDecorator {

        public ImageLazyLodResponseDecorator(ServerWebExchange exchange) {
            super(exchange.getResponse());
        }

        boolean isHtmlResponse(ServerHttpResponse response) {
            return response.getHeaders().getContentType() != null &&
                response.getHeaders().getContentType().includes(MediaType.TEXT_HTML);
        }

        @Override
        @NonNull
        public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
            var response = getDelegate();
            if (!isHtmlResponse(response)) {
                return super.writeWith(body);
            }
            var bodyWrap = Flux.from(body)
                .map(dataBuffer -> {
                    var byteBuffer = ByteBuffer.allocateDirect(dataBuffer.readableByteCount());
                    dataBuffer.toByteBuffer(byteBuffer);
                    DataBufferUtils.release(dataBuffer);
                    return byteBuffer.asReadOnlyBuffer();
                })
                .collectSortedList()
                .flatMap(byteBuffers -> {
                    var html = byteBuffersToString(byteBuffers);

                    return Settings.getBasicConfig(settingFetcher)
                        .switchIfEmpty(Mono.just(new Settings.BasicConfig()))
                        .flatMap(config -> {

                            if (BooleanUtils.isTrue(config.getStatus())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("{} 开始构造图片异步加载属性", LOG_PREFIX);
                                }
                                var optimizedHtml = this.buildImageLazyLoadAttr(html, config);
                                var byteBuffer = stringToByteBuffer(optimizedHtml);
                                return Mono.just(byteBuffer);
                            }
                            return Mono.just(stringToByteBuffer(html));

                        });
                })
                .map(byteBuffer -> response.bufferFactory().wrap(byteBuffer));
            return super.writeWith(bodyWrap);
        }

        /**
         * 构造图片异步加载属性
         */
        private String buildImageLazyLoadAttr(String html, Settings.BasicConfig config) {
            Document document = Jsoup.parse(html);

            document.select(BusConstant.HtmlDoc.IMG).forEach(img -> {

                String src = img.attr(BusConstant.HtmlDoc.SRC);

                String original = img.attr(BusConstant.HtmlDoc.DATA_ORIGINAL);
                if (StringUtils.isBlank(original)) {
                    img.attr(BusConstant.HtmlDoc.DATA_ORIGINAL, src);
                }

                if (StringUtils.isNotBlank(config.getLoadImgUrl())) {
                    img.attr(BusConstant.HtmlDoc.SRC, config.getLoadImgUrl());
                } else {
                    img.removeAttr(BusConstant.HtmlDoc.SRC);
                }

                String loading = img.attr(BusConstant.HtmlDoc.LOADING);
                if (StringUtils.isBlank(loading)) {
                    img.attr(BusConstant.HtmlDoc.LOADING, BusConstant.HtmlDoc.LAZY);
                }

                if (!img.hasClass(BusConstant.HtmlDoc.LAZY)) {
                    img.addClass(BusConstant.HtmlDoc.LAZY);
                }
            });

            return document.outerHtml();
        }
    }

    private String byteBuffersToString(List<ByteBuffer> byteBuffers) {
        int total = byteBuffers.stream().mapToInt(ByteBuffer::remaining).sum();
        ByteBuffer combined = ByteBuffer.allocate(total);

        for (ByteBuffer buffer : byteBuffers) {
            combined.put(buffer);
        }

        combined.flip();
        byte[] byteArray = new byte[combined.remaining()];
        combined.get(byteArray);

        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public ByteBuffer stringToByteBuffer(String str) {
        byte[] byteArray = str.getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.wrap(byteArray);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 90;
    }
}