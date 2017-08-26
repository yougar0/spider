package me.yougar.spider.lib.util;

import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class WebClientUtil {
  // WebClient,　http网络请求
  public static WebClient webClient = WebClient.create(VertxUtil.vertx, new WebClientOptions().
        setKeepAlive(true).setMaxPoolSize(5).setConnectTimeout(3000).setIdleTimeout(5).setReuseAddress(true));
}
