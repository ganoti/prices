# prices

The PricesAggregatorApp aggregates XML files that food-store chains publish on the web as a part of the
“prices-transparency” new law in Israel. This app is executed every hour on the Hebrew University servers. 
It runs for a given chain, and stores the XML files that were published in the chain’s website in that day, 
and have not yet been aggregated earlier in the same day in previous application runs. 

Table of Contents: 
* Requirements
* Program arguments
* Output
* Things we need TODO

Requirements: 
* Java 1.7
* Add to class path all jars under lib/htmlunit-216

Program arguments: 
* aggtype: the aggregation type, can be either “daily” or “hourly”. Both types are executed every hour, and aggregate 
all XML files published in the current day and have not yet been aggregated. “Daily” type aggregates files with names starting with “PricesFull”, “PromosFull” and “Stores”, while “hourly” type aggregates files with names starting with 
“Prices” and “Promos” (these files should contain prices and promos differences occurred the same day).
* chain: the food-store chain to aggregate its XML files. Possible values and their status:

  * rami: aggregates Rami-Levi files, given by Cerberus.
    * URL: https://url.retail.publishedprices.co.il/ (Cerberus)
    * Implementation status: complete
    * Provided files status: Not-provided (for a few days already).
  * dosh: aggregates Super-Dosh files, given by Cerberus.
    * URL: https://url.retail.publishedprices.co.il/ (Cerberus)
    * Implementation status: complete
    * Provided files status: complete
  * tiv: aggregates Tiv-Taam files, given by Cerberus.
    * URL: https://url.retail.publishedprices.co.il/ (Cerberus)
    * Implementation status: complete
    * Provided files status: Not-provided. There are no updated files to download.
  * dor: aggregates Doralon files, given by Cerberus.
    * URL: https://url.retail.publishedprices.co.il/ (Cerberus)
    * Implementation status: complete
    * Provided files status: Seems complete, but today, for instance, provided files with names having “NULL” prefix.
  * coop: aggregates Co-Op files.
    * URL: (1) prices from: http://coopisrael.coop/home/prices; (2) promos from: http://coopisrael.coop/home/promos; (3) stores from: http://coopisrael.coop/home/branches;
    * Implementation status: complete, but notice that both daily and hourly aggregate the “full” files, since no partial files are provided.
    * Provided files status: files are generated online upon request, and only in the full-mode type. Still missing: the partial and smaller prices and promos files that contain only changes. 
  * shuf: aggregates Shufersal files.
    * URL: http://prices.shufersal.co.il/
    * Implementation status: often internal error server and timeout exceptions are thrown mainly when choosing a different Category on page. 
    * Provided files status: seems complete, but not usable due to the exceptions on server. 
  * hazi: aggregates Hazi-Hinam files, given by Cerberus.
    * URL: https://url.retail.publishedprices.co.il/ (Cerberus)
    * Implementation status: complete
    * Provided files status: complete
  * keshet: aggregates Keshet-Teamim files, given by Cerberus.
    * URL: https://url.retail.publishedprices.co.il/ (Cerberus)
    * Implementation status: complete
    * Provided files status: complete
  * yohan: aggregates Yohananof files, given by Cerberus.
    * URL: https://url.retail.publishedprices.co.il/ (Cerberus)
    * Implementation status: complete
    * Provided files status: (seems) complete
  * osher: aggregates Osher-Ad files, given by Cerberus.
    * URL: https://url.retail.publishedprices.co.il/ (Cerberus)
    * Implementation status: currently assumes the files-table is in a single page (which is the case in the rest of the Cerberus chains) however needs proceed to the next page if table continues on different pages. 
    * Provided files status: complete
  * victory: aggregates Victory files, given by Nibit.
    * URL: http://matrixcatalog.co.il/NBCompetitionRegulations.aspx (Nibit)
    * Implementation status: Incomplete. Need to be fixed: while iterating the table lines and clicking the download links, only the first link triggers the webWindowContentChanged method of the WebClient, and the others are not and are thus not saved. README is not yet provided.
    * Provided files status: (seems) complete. However, they should align and provide the standard name-files. 
  * lahav: aggregates “Mahsanei-Lahav” files, given by Nibit.
    * URL: http://matrixcatalog.co.il/NBCompetitionRegulations.aspx (Nibit)
    * Implementation status: like “victory” chain. 
    * Provided files status:  like “victory” chain. 
  * hashuk: aggregates “Mahsanei-Hashuk” files, given by Nibit.
    * URL: http://matrixcatalog.co.il/NBCompetitionRegulations.aspx (Nibit)
    * Implementation status: like “victory” chain. 
    * Provided files status: like “victory” chain. 
  * mega: aggregates Mega files.
    * URL: http://publishprice.mega.co.il/ + the current date
    * Implementation status: complete 
    * Provided files status: complete (their system is the most convenient, full and stable.)
  * bitan: aggregates Yeinot-Bitan files.
    * URL: http://www.ybitan.co.il/pirce_update
    * Implementation status: complete
    * Provided files status: Not provided (only old files appear).
  * eden: aggregates Eden-Teva files.
    * URL: http://operations.edenteva.co.il/Prices/index 
    * Implementation status: not implemented yet 
    * Provided files status: Not provided (only old files appear). 

Output: 
Given a chain, the application saves the (extracted) XML files, to the following path: 
* If aggtype=daily: Save PricesFull and PromoFull files by their branch to “daily/yyyyMMdd/chain/branchId/”, and Stores, README, and error-log files to “daily/yyyyMMdd/chain/”.
* If aggtype=hourly: Save Prices and Promo files by their branch to “hourly/yyyyMMdd/HH/chain/branchId/”, and README, and error-log files to “hourly/yyyyMMdd/HH/chain/”.

TODO: – and we will love to have your help! 
* Finish implementation according to above “Implementation status” per chain. This is mainly fixing Nibit three chains (victory, hashuk & lahav), fixing Shufersal, and implementing Eden. 
* Error logging: now a basic error-logging is provided (per aggtype and chain), and saved under each chain directory. It still require further XML checking (format and content), and an automated way to summarize problems in aggregation and send them to the admin to take care. 
