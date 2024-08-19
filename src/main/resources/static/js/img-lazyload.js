jQuery(document).ready(function () {
    // 初始化懒加载插件
    jQuery("img.lazy").lazyload({
        effect: "fadeIn",
        threshold: 50,
        failure_limit : 5
    });
});
