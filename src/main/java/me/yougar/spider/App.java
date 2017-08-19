package me.yougar.spider;

import me.yougar.spider.lib.Spider;
import org.jsoup.select.Elements;
import java.rmi.server.UID;

public class App {
    public static void main( String[] args ) {
        Spider spider = new Spider();
        spider.setExecMillSecondsTimeInterval(1000);
        for (int i = 1; i <= 91; i++) {
          String url = String.format("http://tu.hanhande.com/scy/scy_%d.shtml", i);
          spider.get(url, ar -> {
            if (ar.succeeded()) {
              spider.infoLog(url);
              Elements links = spider.select(ar, "body div.main div.content ul li p a");
              links.forEach(link -> {
                String imagesFolder = String.format("images/%s_%s", link.text(), new UID().toString());
                spider.getFileSystem().mkdir(imagesFolder, ar2 -> {
                  if (ar2.succeeded()) {
                    String href = link.attr("href");
                    spider.get(href, ar3 -> {
                      if (ar3.succeeded()) {
                        spider.infoLog(href);
                        Elements images = spider.select(ar3, "html body div.main div.content div.picshow div.picshowlist div.picshowlist_mid div.picmidmid ul#picLists li a img");
                        images.forEach(image -> {
                          String imageSrc = image.attr("src");
                          try {
                            String imageFile = imageSrc.substring(38);
                            spider.get(imageSrc, ar4 -> {
                              if (ar4.succeeded()) {
                                spider.infoLog(imageSrc);
                                String file = String.format("%s/%s", imagesFolder, imageFile);
                                spider.getFileSystem().writeFile(file, ar4.result().body(), ar5 -> {
                                  if (ar5.failed()) {
                                    spider.error(String.format("Write iamge file %s failed", file));
                                  }
                                });
                              } else {
                                spider.errorLog(imageSrc, ar4.cause().getMessage());
                              }
                            });
                          } catch(IndexOutOfBoundsException e) {
                          }
                        });
                      } else {
                        spider.errorLog(href, ar3.cause().getMessage());
                      }
                    });
                  }
                });
              });
            } else {
              spider.errorLog(url, ar.cause().getMessage());
            }
          });
        }
        spider.run();
        Spider.getVertx().close();
    }
}
