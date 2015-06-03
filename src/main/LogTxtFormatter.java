package main;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/*
 * Format parts of a log record to a single line
 */
public class LogTxtFormatter extends Formatter {

  public String format(LogRecord rec) {
    StringBuilder sb = new StringBuilder();
    sb.append(calcDate(rec.getMillis())).append("\t");
    sb.append(rec.getLevel()).append(":\t");
    sb.append(rec.getMessage()).append(System.getProperty("line.separator"));

    return sb.toString();
  }

  private String calcDate(long millisecs) {
    SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
    Date resultdate = new Date(millisecs);
    return date_format.format(resultdate);
  }

}
