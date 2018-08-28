package bf.car;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class JsoupUtil {
  private static Logger logger = Logger.getLogger(JsoupUtil.class);

  static Document urlToDoc(String domain, String path) throws IOException {
    if(path.startsWith("/")) path = path.substring(1);
    String[] split = domain.split("\\.");
    String root = split[1];
    String folder = "cache/" + root + "/" + split[0] + "/";
    Files.createDirectories(Paths.get(folder));
    String fileName = folder + path.replace("/", "_");
    if (!fileName.contains(".")) {
      fileName += ".html";
    }
    String url = "http://" + domain + "/" + path;

    Document doc;
    if (new File(fileName).exists()) {
      String content = FileUtil.readToString(fileName);
      if (content == null) return null;
      doc = Jsoup.parse(content);
    } else {
      logger.debug(url);
      doc = Jsoup.connect(url).get();
      FileUtil.writeToFile(fileName, doc.html());
    }

    return doc;
  }
}
