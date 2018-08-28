package bf.car;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;

import org.apache.log4j.Logger;


class FileUtil {
  private static Logger logger = Logger.getLogger(FileUtil.class);

  static String readToString(String fileName) {
    String content = null;
    try {
      content = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
    } catch (FileNotFoundException e) {
      logger.error(e);
    }
    return content;
  }

  static void writeToFile(String fileName, String content) {
    logger.debug(fileName);
    try (Writer writer1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
      writer1.write(content);
    } catch (IOException e) {
      logger.error(e);
    }
  }

}
