package bf.car;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;

public class UsNewsMakeModelYear {

  private static Logger logger = Logger.getLogger(UsNewsMakeModelYear.class);

  public static void main(String[] args) throws Exception {
    String fileName = "usnews/make-model-year.json";
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("make-model-year.csv"));
         CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
         Stream<String> stream = Files.lines(Paths.get(fileName))) {

      stream.filter(l -> l.contains("\"url\""))
            .forEach(l -> {
              String car = l.split("\"url\": \"/cars-trucks/")[1].split("\",")[0];
              if (l.contains("200")) return;
              logger.debug(l);
              String[] split = car.split("/");
              String make = split[0].replace("-", " ");
              if (make.equals("hummer")
                      || make.equals("mercury")
                      || make.equals("saturn")
                      || make.equals("scion")
                      || make.equals("pontiac")) return;
              String model = split[1].replace("-", " ");

              String year = "";
              if (split.length == 3) {
                year = split[2];
                if (year.equals("2019")) return;
              }

              if (split.length == 2) {
                year = "2018";
              }
              logger.debug(String.format("%s %s %s", make, model, year));
              try {
                csvPrinter.printRecord(year, make, model);
              } catch (IOException e) {
                logger.error(e);
              }
            });

      csvPrinter.flush();
    }
  }

}
