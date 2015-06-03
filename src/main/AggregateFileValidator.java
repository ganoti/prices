package main;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import main.PricesAggregatorApp.AggregatorType;
import main.PricesAggregatorApp.ChainName;

public class AggregateFileValidator {
  Map<String, Set<String>> downloadedFilesByBranch;

  public AggregateFileValidator(AggregatorType aggType, ChainName chainName,
      String sessionDir) {

    downloadedFilesByBranch =
        getDownloadedFilesByBranch(aggType, chainName, sessionDir);

  }

  public boolean shouldAggregateFile(String fileName) {
    if (!isFromToday(fileName))
      return false;

    String branchId = getBranchId(fileName);
    return (!downloadedFilesByBranch.containsKey(branchId) || !downloadedFilesByBranch
        .get(branchId).contains(
            fileName.substring(0, fileName.lastIndexOf("."))));
  }

  public static synchronized String getBranchId(String fileName) {
    try {
      return fileName.split("-")[1];
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Determine whether row is from today based on the date in the last part of
   * the rowId. Assumes rowId is in the following format:
   * (Prices|Promo)[Full]<chainId>-<branchId>-<datetime>.gz
   * 
   * @param row
   * @return
   */
  private boolean isFromToday(String rowId) {
    try {
      String[] t = rowId.split("-");
      String rowDate;

      if (t.length == 3)
        rowDate = t[2].substring(0, 8);
      else if (t.length == 2
          && rowId.startsWith(PricesAggregatorApp.PREFIX_STORES))
        rowDate = t[1].substring(0, 8);
      else
        return false;

      SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyyMMdd");
      return dayDateFormat.format(new Date()).equals(rowDate);

    } catch (Exception e) {
      return false;
    }

  }

  private synchronized Map<String, Set<String>> getDownloadedFilesByBranch(
      AggregatorType aggType, ChainName chainName, String sessionDir) {
    Map<String, Set<String>> downloadedFilesByBranch =
        new HashMap<String, Set<String>>();

    File todayFolder =
        new File(PricesAggregatorApp.getDaySubSessionPath(sessionDir));
    File[] foldersToSearch;
    if (aggType.equals(AggregatorType.hourly))
      foldersToSearch = todayFolder.listFiles();
    else if (aggType.equals(AggregatorType.daily))
      foldersToSearch = new File[] { todayFolder };
    else
      throw new RuntimeException("UnSupported aggType " + aggType);

    if (foldersToSearch != null) {
      File[] branchesFolders;
      File[] downloadedFiles;
      for (File hourFolder : foldersToSearch) {
        branchesFolders =
            new File(hourFolder.getPath() + "/" + chainName).listFiles();
        if (branchesFolders != null) {
          for (File branch : branchesFolders) {
            downloadedFiles = branch.listFiles(getPricesOrPromosFileFilter());
            if (downloadedFiles != null) {
              for (File file : downloadedFiles) {
                if (!downloadedFilesByBranch.containsKey(branch.getName())) {
                  downloadedFilesByBranch.put(branch.getName(),
                      new HashSet<String>());
                }
                // put without extension, because these are xmls that later
                // are compared with gz files
                downloadedFilesByBranch.get(branch.getName()).add(
                    file.getName()
                        .substring(0, file.getName().lastIndexOf(".")));
              }
            }
          }
        }
      }
    }

    return downloadedFilesByBranch;
  }

  private synchronized FileFilter getPricesOrPromosFileFilter() {
    return new FileFilter() {
      @Override
      public boolean accept(File pathname) {

        return pathname.getName().startsWith("Prices")
            || pathname.getName().startsWith("Promo");
      }
    };
  }
}
