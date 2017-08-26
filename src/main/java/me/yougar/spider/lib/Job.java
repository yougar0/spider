package me.yougar.spider.lib;

// 一个闭包,把相关的代码封进去

@FunctionalInterface
public interface Job {
  public void handle();
}
