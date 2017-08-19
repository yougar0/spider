package me.yougar.spider;

import me.yougar.spider.lib.Spider;
import org.jsoup.select.Elements;
import java.rmi.server.UID;

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

        Spider spider = new Spider();
        spider.setExecMillSecondsTimeInterval(1000);
        spider.setMaxPriority(3);
        for (int i = start; i <= end; i++) {
          String url = String.format("http://tu.hanhande.com/scy/scy_%d.shtml", i);
          spider.get(1, url, ar -> {
            if (ar.succeeded()) {
              if (ar.result().statusCode() != 200) {
                spider.error(String.format("When get %s return status code %d", url, ar.result().statusCode()));
                return;
              }
              spider.infoLog(url);
              Elements links = spider.select(ar, "body div.main div.content ul li p a");
              links.forEach(link -> {
                String imagesFolder = String.format("images/%s_%s", link.text(), new UID().toString());
                spider.getFileSystem().mkdir(imagesFolder, ar2 -> {
                  if (ar2.succeeded()) {
                    String href = link.attr("href");
                    spider.get(2, href, ar3 -> {
                      if (ar3.succeeded()) {
                        spider.infoLog(href);
                        Elements images = spider.select(ar3, "html body div.main div.content div.picshow div.picshowlist div.picshowlist_mid div.picmidmid ul#picLists li a img");
                        images.forEach(image -> {
                          String imageSrc = image.attr("src");
                          try {
                            String imageFile = imageSrc.substring(38);
                            spider.get(3, imageSrc, ar4 -> {
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
