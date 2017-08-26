package me.yougar.spider.lib;

import java.util.concurrent.ConcurrentLinkedQueue;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.Logger;
import java.util.ArrayList;
import me.yougar.spider.lib.util.WebClientUtil;
import me.yougar.spider.lib.util.VertxUtil;

public class Dispatcher {

  private ArrayList<ConcurrentLinkedQueue<Job>> jobs = new ArrayList();

  private int execMillSecondsTimeInterval;

  private Logger logger = LoggerFactory.getLogger(Dispatcher.class);

  private int priority;

  private int hit; // 触发次数

  public void setExecMillSecondsTimeInterval(int millSeconds) {
    this.execMillSecondsTimeInterval = millSeconds;
  }

  public void setMaxPriority(int priority) {
    this.priority = priority;
    // 按照设置的优先级配置并发队列
    while (priority > 0) {
      jobs.add(new ConcurrentLinkedQueue<Job>());
      priority--;
    }
  }

  public void addJob(int priority, Job job) {
    jobs.get(priority - 1).add(job);
  }

  public void setHit(int hit) {
    this.hit = hit;
  }

  private void dispatch() {
      for (int level = priority - 1; level >= 0; level--) {

        Job job = jobs.get(level).poll();
        if (job != null) {
          job.handle();
          break;
        }
        // 当任任务消费完后,消费hit
        if (level == 0) {
          hit--;
        }
    }
    //　设置的触发次数满足后,结束进程
    if (hit == 0) {
      WebClientUtil.webClient.close();
      VertxUtil.vertx.close();
    }
  }

  public void run() {

    // 利用vertx设置定时执行器,每隔１秒执行一次调度
    VertxUtil.vertx.setPeriodic(1000, l -> {
      dispatch();
    });

  }

}
