package me.yougar.spider.lib.util;

import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

public class VertxUtil {
  public static Vertx vertx = Vertx.vertx(); // 创建vertx实例,其他模块调用
  public static FileSystem fileSystem = vertx.fileSystem();
}
