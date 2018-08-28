package bf.car;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Autotk {

  private static Logger logger = Logger.getLogger(Autotk.class);

  public static void main(String[] args) throws Exception {
    String domain = "autotk.com";
    String path = "0-60-times.html";

    Document doc = JsoupUtil.urlToDoc(domain, path);
    if (doc == null) return;
    Elements makes = doc.select(".all-makes a[title~=0-60 times]");

    try (
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(domain + ".csv"));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)
    ) {


      for (Element make : makes) {
        Document makeDoc = JsoupUtil.urlToDoc(domain, make.attr("href"));
        if (makeDoc == null) continue;
        Elements models = makeDoc.select(".table-container td a");
        for (Element model : models) {
          String href = model.attr("href");
          Document modelDoc = JsoupUtil.urlToDoc(domain, href);
          if (modelDoc == null) continue;
          //body > main > div.l-col1 > section:nth-child(2) > h2
          Elements trims = modelDoc.select(".table-container");
          for (Element trim : trims) {

            Elements yearMakeModel = trim.select("h2");
            String yearMakeModelString = yearMakeModel.first().childNode(0).outerHtml();

            String finder = "0-60 times, all trims";
            if (!yearMakeModelString.contains(finder)) continue;

            String[] split = yearMakeModelString.split(finder)[0].split(" ");
            String year = split[0];
            String makeName = split[1];
            String modelName = split[2];
            Elements rows = trim.select("tr");
            for (Element row : rows) {
              if (row.html().contains("Trim, HP, Engine, Transmission")) continue;
              Elements aTrim = row.select("td");
              Element first = aTrim.first();
              String trimName = first.childNode(0).outerHtml().trim();
              Element timeElement = first.nextElementSibling();
              String time = timeElement.children().first().childNode(0).outerHtml().split("sec")[0].trim();
              logger.debug(String.format("year:%s make:%s model:%s trim:%s time:%s",
                                         year, makeName, modelName, trimName, time));
              csvPrinter.printRecord(year, makeName, modelName, trimName, time);
            }
          }
        }
      }
      csvPrinter.flush();
    }
  }
}
