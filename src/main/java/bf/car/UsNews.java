package bf.car;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UsNews {

  private static Logger logger = Logger.getLogger(UsNews.class);

  public static void main(String[] args) throws Exception {
    List<String> lines = Files.readAllLines(Paths.get("make-model-year.csv"));

    try (
            BufferedWriter writer = Files.newBufferedWriter(Paths.get("usnews.csv"));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)
    ) {
      lines.stream().parallel().forEach(line -> {
        String[] car = line.split(",");
        String year = car[0];
        String make = car[1].replace(" ", "-");
        String model = car[2].replace(" ", "-");

        String url = String.format("https://cars.usnews.com/cars-trucks/%s/%s", make, model);
        if (!year.equals("2018")) url += "/" + year;
        String fileName = String.format("usnews/%s_%s_%s", make, model, year) + ".html";

        Document doc = null;

        if (new File(fileName).exists()) {
          String content = null;
          try {
            content = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
          } catch (FileNotFoundException e) {
            logger.error(e);
          }
          if (content == null) return;
          doc = Jsoup.parse(content);
        } else {
          try {
            doc = Jsoup.connect(url)
                       .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36")
                       .get();
            String html = doc.html();
            try (Writer writer1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
              writer1.write(html);
            }
          } catch (Exception e) {
            logger.warn(e);
          }
        }
        if (doc == null) return;
        Elements mpgs = doc.select("#mpg > div > strong");

        String mpg1 = "";
        String mpg2 = "";
        String mpg3 = "";
        String mpg4 = "";
        for (int i = 0; i < mpgs.size(); i++) {
          Element mpg = mpgs.get(i);
          if (i == 0) mpg1 = mpg.html();
          else if (i == 1) mpg2 = mpg.html();
        }

        if (mpg1.contains("-") || mpg2.contains("-")) {
          String[] split1 = mpg1.split("-");
          String[] split2 = mpg2.split("-");
          mpg1 = split1[0];
          mpg2 = split2[0];

          if (split1.length == 1)
            mpg3 = split1[0];
          else
            mpg3 = split1[1];

          if (split2.length == 1)
            mpg4 = split2[0];
          else
            mpg4 = split2[1];
        }

        Elements overall = doc.select(".scorecard__score");
        Elements reliability = doc.select(".reliability");
        Elements values = doc.select(".scorecard__value-label");
        boolean isSafety = false;
        String safety = "";
        for (Element value : values) {
          String val = value.html();
          if (isSafety) {
            safety = val;
            isSafety = false;
          }
          if (val.equals("Safety:")) {
            isSafety = true;
          }
        }
        logger.debug(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", year, make, model,
                                   overall.html(),
                                   safety,
                                   reliability.attr("alt"),
                                   mpg1, mpg2, mpg3, mpg4));
        try {
          csvPrinter.printRecord(year, make, model,
                                 format(overall.html()), format(safety), format(reliability.attr("alt")),
                                 format(mpg1), format(mpg2), format(mpg3), format(mpg4));
        } catch (IOException e) {
          logger.error(e);
        }
      });
      csvPrinter.flush();
    }
  }

  private static String format(String in) {
    if (in.equals("N/A")) return "-1";
    if (in.equals("TBD")) return "-1";
    return in;
  }
}
