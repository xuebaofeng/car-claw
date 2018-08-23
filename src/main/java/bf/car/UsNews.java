package bf.car;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static bf.car.ZeroTo60.ZERO_60;

public class UsNews {

  private static Logger logger = Logger.getLogger(UsNews.class);

  public static void main(String[] args) throws Exception {
    Set<String> urls = new HashSet<>();
    try (Stream<String> lines = Files.lines(Paths.get(ZERO_60));
         BufferedWriter writer = Files.newBufferedWriter(Paths.get("usnews.csv"));
         CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)
    ) {
      for (String line : (Iterable<String>) lines::iterator) {
        line = line.replace("&nbsp;", ",");
        String[] car = line.split(",");
        String yearStr = car[0];
        int year = Integer.parseInt(yearStr);
        if (year < 2016) continue;

        String make = car[1];
        if (make.equals("audi")
                || make.equals("bmw")
                || make.equals("bentley")
                || make.equals("bugatti")
                || make.equals("alfa")
                || make.equals("aston")
                || make.equals("cadillac")
                || make.equals("callaway")
                || make.equals("caterham")
                || make.equals("citroen")
                || make.equals("ferrari")
                || make.equals("fiat")
                || make.equals("jaguar")
                || make.equals("jeep")
                || make.equals("koenigsegg")
                || make.equals("lamborghini")
                || make.equals("land")
                || make.equals("lotus")
                || make.equals("range")
                || make.equals("mercedes")
                || make.equals("maserati")
                || make.equals("mclaren")
                || make.equals("morgan")
                || make.equals("peugeot")
                || make.equals("proton")
                || make.equals("skoda")
                || make.equals("ram")
                || make.equals("renault")
                || make.equals("seat")
                || make.equals("smart")
                || make.equals("ssangyong")
                || make.equals("suzuki")
                || make.equals("tesla")
                || make.equals("vauxhall")
                || make.equals("volvo")
                || make.equals("ariel")) continue;
        String model = car[2];

        if (model.equals("santa")) {
          model = car[2] + "-" + car[3];
        }

        if (make.equals("mazda")) {
          if (model.length() == 1)
            model = "mazda" + model;
          else if (model.equals("mx-5")) {
            model = "mx-5-miata";
          }
        }

        String url = String.format("https://cars.usnews.com/cars-trucks/%s/%s/%s", make, model, yearStr);
        String fileName = String.format("usnews/%s_%s_%s", make, model, yearStr) + ".html";

        if (urls.contains(url)) continue;
        urls.add(url);

        Document doc;

        if (new File(fileName).exists()) {
          String content = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
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
          } catch (HttpStatusException e) {
            logger.warn(e);
            continue;
          }
        }

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
/*
        logger.debug(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", year, make, model,
                                   overall.html(),
                                   safety,
                                   reliability.attr("alt"),
                                   mpg1, mpg2, mpg3, mpg4));*/
        csvPrinter.printRecord(year, make, model,
                               format(overall.html()), format(safety), format(reliability.attr("alt")),
                               format(mpg1), format(mpg2), format(mpg3), format(mpg4));
      }
      csvPrinter.flush();
    }
  }

  private static String format(String in) {
    if (in.equals("N/A")) return "-1";
    if (in.equals("TBD")) return "-1";
    return in;
  }
}
