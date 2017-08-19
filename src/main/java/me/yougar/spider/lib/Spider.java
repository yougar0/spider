package me.yougar.spider.lib;

import java.util.concurrent.ConcurrentLinkedQueue;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.core.Vertx;
import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import java.util.ArrayList;

public class Spider {

  private static Vertx vertx = Vertx.vertx();

  private WebClient client;

  private ArrayList<ConcurrentLinkedQueue<Job>> jobs = new ArrayList();

  private int execMillSecondsTimeInterval;

  private FileSystem fs;

  private Logger logger = LoggerFactory.getLogger(Spider.class);

  private int priority;

  public Spider() {
    WebClientOptions options = new WebClientOptions().setKeepAlive(true).setMaxPoolSize(5).setConnectTimeout(3000);
    client = WebClient.create(vertx, options);
    fs = vertx.fileSystem();
  }

  public void setExecMillSecondsTimeInterval(int millSeconds) {
    this.execMillSecondsTimeInterval = millSeconds;
  }

  public void setMaxPriority(int priority) {
    this.priority = priority;
    while (priority > 0) {
      jobs.add(new ConcurrentLinkedQueue<Job>());
      priority--;
    }
  }

  public void get(int _priority, String url, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    Job job = () -> {
      client.getAbs(url).send(handler);
    };
    jobs.get(_priority - 1).add(job);
  }

  public Elements select(AsyncResult<HttpResponse<Buffer>> ar, String selector) {
    return Jsoup.parse(ar.result().bodyAsString("gb2312")).select(selector);
  }

  public void infoLog(String url) {
    logger.info(String.format("GET %s succeeded!", url));
  }

  public void errorLog(String url, String message) {
    logger.error(String.format("GET %s failed! Error: %s", url, message));
  }

  public void error(String errorMessage) {
    logger.error(errorMessage);
  }

  public void run() {
    boolean isFinished = false;

    while(!isFinished) {
      for (int level = priority - 1; level >= 0; level--) {
        Job job = jobs.get(level).poll();
        if (job != null) {
          job.handle();
          break;
        }

        if (level == 0) {
          try {
            Thread.sleep(4000);
          } catch(Exception ex) {}

          int _level = level;
          while(_level < priority) {
            Job j = jobs.get(_level).poll();
            if ( j != null) {
              j.handle();
              break;
            }
            if (_level == priority - 1) {
              isFinished = true;
            }
            _level++;
          }
        }
      }

      try {
        Thread.sleep(execMillSecondsTimeInterval);
      } catch(Exception e) {

      }
    }
    logger.info("All jobs is done!");
    client.close();
  }

  public FileSystem getFileSystem() {
    return fs;
  }

  public static Vertx getVertx() {
    return vertx;
  }
}
