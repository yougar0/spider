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

public class Spider {

  private static Vertx vertx = Vertx.vertx();

  private WebClient client;

  private ConcurrentLinkedQueue<Job> jobs = new ConcurrentLinkedQueue();

  private int execMillSecondsTimeInterval;

  private FileSystem fs;

  private Logger logger = LoggerFactory.getLogger(Spider.class);

  public Spider() {
    WebClientOptions options = new WebClientOptions().setKeepAlive(true).setMaxPoolSize(5).setConnectTimeout(3000);
    client = WebClient.create(vertx, options);
    fs = vertx.fileSystem();
  }

  public void setExecMillSecondsTimeInterval(int millSeconds) {
    this.execMillSecondsTimeInterval = millSeconds;
  }

  public void get(String url, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    Job job = () -> {
      client.getAbs(url).send(handler);
    };
    jobs.add(job);
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
    Job job = jobs.poll();
    while(job != null) {
      job.handle();
      try {
        Thread.sleep(execMillSecondsTimeInterval);
      } catch(Exception e) {

      }
      job = jobs.poll();
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
