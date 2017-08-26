package me.yougar.spider;

import io.vertx.core.AsyncResult;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.codec.BodyCodec;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.rmi.server.UID;
import io.vertx.core.file.OpenOptions;

import me.yougar.spider.lib.Dispatcher;
import me.yougar.spider.lib.util.WebClientUtil;
import me.yougar.spider.lib.util.VertxUtil;

public class Handler {

  private static Logger logger = LoggerFactory.getLogger(Handler.class);

  private static void info(String url, int statusCode) {
    logger.info(String.format("Get %s with statusCode %d", url, statusCode));
  }

  // 处理第一阶段的任务
  public static void handle(String url, Dispatcher dispatcher) {
    WebClientUtil.webClient.getAbs(url).as(BodyCodec.string("gb2312")).send(asyncResult -> {
      if (asyncResult.succeeded()) {
        HttpResponse<String> res = asyncResult.result();
        if (res.statusCode() != 200) {
          logger.error(String.format("When get %s return status code %d", url, res.statusCode()));
          return;
        }
        // 记录请求过程
        info(url, res.statusCode());

        // 解析Html内容
        Document root = Jsoup.parse(res.body());
        Elements links = root.select("body div.main div.content ul li p a");
        links.forEach(link -> {
          handle(link, dispatcher);
        });
      } else {
        logger.error(asyncResult.cause());
      }
    });
  }

  // 生成第二阶段任务
  private static void handle(Element link, Dispatcher dispatcher) {
    // 生成存储图片的目录, 后面的UID保证该目录的唯一性
    String imagesFolder = String.format("images/%s_%s", link.text(), new UID().toString());
    // 创建该目录
    VertxUtil.fileSystem.mkdir(imagesFolder, asyncResult -> {
      if (asyncResult.succeeded()) {
        String href = link.attr("href");
        dispatcher.addJob(2, () -> {
          handle(href, imagesFolder, dispatcher);
        });
      } else {
        logger.error(asyncResult.cause());
      }
    });
  }

  // 处理第二阶段任务,并生成第三阶段任务
  private static void handle(String href, String imagesFolder, Dispatcher dispatcher) {
    WebClientUtil.webClient.getAbs(href).as(BodyCodec.string("gb2312")).send(asyncResult -> {
      if (asyncResult.succeeded()) {
        HttpResponse<String> res = asyncResult.result();
        info(href, res.statusCode());

        // 解析html生成图片的链接
        Document root = Jsoup.parse(res.body());
        Elements images = root.select("html body div.main div.content div.picshow div.picshowlist div.picshowlist_mid div.picmidmid ul#picLists li a img");
        images.forEach(image -> {
          String imageSrc = image.attr("src");
          try {
            // 生成图片的名字
            String imageName = imageSrc.substring(38);
            dispatcher.addJob(3, () -> {
              handle(imageName, imageSrc, imagesFolder, dispatcher);
            });
          } catch(Exception e) {
            logger.error(e);
          }
        });
      } else {
        logger.error(asyncResult.cause());
      }
    });
  }

  // 下载并存储图片
  private static void handle(String imageName, String imageSrc, String imagesFolder, Dispatcher dispatcher) {
    // 首先创建一个异步文件流
    VertxUtil.fileSystem.open(String.format("%s/%s", imagesFolder, imageName), new OpenOptions().setCreate(true).setWrite(true), asyncFile -> {
      if (asyncFile.succeeded()) {
        // 下载图片并保存图片
        WebClientUtil.webClient.getAbs(imageSrc).as(BodyCodec.pipe(asyncFile.result())).send(asyncResult -> {
          if (asyncResult.failed()) {
            logger.error(asyncResult.cause());
          }
        });
      } else {
        logger.error(asyncFile.cause());
      }
    });
  }
}
