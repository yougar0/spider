package me.yougar.spider;

import me.yougar.spider.lib.Dispatcher;

public class App {
    public static void main( String[] args ) {
        if (args.length != 2) {
          System.err.println("Error: Require two params");
          return;
        }

        int start, end;
        try {
          start = Integer.parseInt(args[0]);
          end = Integer.parseInt(args[1]);
        } catch(Exception e) {
          System.err.println(e);
          return;
        }

        if (start <= 0 || end <= 0 || start > end) {
          System.err.println("Error: Input params is invalid!");
          return;
        }

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setExecMillSecondsTimeInterval(1000);
        dispatcher.setMaxPriority(3);
        // hit 6次以后结束进程
        dispatcher.setHit(6);
        // 开始抓取网页
        for (int i = start; i <= end; i++) {
          String url = String.format("http://tu.hanhande.com/scy/scy_%d.shtml", i);
          // 设置第一阶段的任务
          dispatcher.addJob(1, () -> {
            Handler.handle(url, dispatcher);
          });
        }
        // 开始调度执行任务
        dispatcher.run();
    }
}
