package bf.car;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ZeroTo60 {

  public static final String ZERO_60 = "0-60.csv";
  private static Logger logger = Logger.getLogger(ZeroTo60.class);

  public static void main(String[] args) throws IOException {
    Document doc = Jsoup.connect("https://www.zeroto60times.com/browse-by-make/").get();
    Elements elements = doc.select("#browseByMake > li > a");
    try (
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(ZERO_60));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
    ) {
      for (Element element : elements) {
        String makeLink = element.attr("href");
        logger.debug(makeLink);
        Document document = Jsoup.connect(makeLink).get();
        Elements cars = document.select(".statContainer");
        for (Element car : cars) {
//        logger.debug(car);
          String title = car.select(".statTitle").html();
          logger.debug(title);
          String[] details = title.trim().split(" ");
          String year = details[0];
          if(year.endsWith("`")){
            year=year.substring(0,4);
          }
          logger.debug(year);
          String make = details[1];
          logger.debug(make);
          String model = "";
          if (details.length > 2)
            model = details[2];
          logger.debug(model);
          String trim = "";
          if (details.length > 3) {
            trim = details[3];
            logger.debug(trim);
          }
          Elements subStats = car.select(".subStat");
          String time = car.select(".statTimes").html().trim().split(" ")[2];
          if (time.endsWith("<span")) {
            time = time.split("<span")[0];
          }
          logger.debug(time);
          if (time.equals("Be")) continue;
          String drive = "";
          for (Element subStat : subStats) {
            String stat = subStat.html().trim();
            if (stat.equals("FWD") || stat.equals("AWD"))
              drive = stat;
            logger.debug(drive);
          }

          csvPrinter.printRecord(year, make.toLowerCase(), model.toLowerCase(), trim.toLowerCase(), drive.toLowerCase(), time);
        }
      }
      csvPrinter.flush();
    }
  }
}
