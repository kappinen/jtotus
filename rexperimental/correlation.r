breakFlag <- FALSE
for (i in stockNames) for(y in stockNames) {
  if (i == y || breakFlag == T) {
    next
  }

  endDate <- format(Sys.Date(), "%d-%m-%Y")
  array <- c(paste(i, "CLOSE", sep=","), paste(y, "CLOSE", sep=","))
#  data <- fetcher$fetchStockData("01-01-2011", "19-07-2011", .jarray(array, dispatch=F))
  a <- fetcher$fetchPeriodByString(i, "01-01-2011", endDate, "CLOSE");
  b <- fetcher$fetchPeriodByString(y, "01-01-2011", endDate, "CLOSE");
  a.stat <- diff(log(a));
  b.stat <- diff(log(b));

  par(mfrow=c(2, 1))
  plot(gtools::running(a.stat, b.stat, fun=cor), type="l", xlab=paste(i,y, sep="-"))
  ccf(a.stat, b.stat)
  corr <- cor.test(a.stat, b.stat, method="spearman")

  print(paste("Done", i, y, " Cor:", corr$estimate))
  cmd <- readline()
  if (cmd == "q") {
    breakFlag <- TRUE
    break
  }
}

stockNames
a <- fetcher$fetchPeriodByString("Metso Oyj", "01-01-2011", endDate, "VOLUME");
b <- fetcher$fetchPeriodByString("KONE Oyj", "01-01-2011", endDate, "VOLUME");

a.stat <- diff(log(a));
b.stat <- diff(log(b));

corr <- cor.test(a.stat, b.stat, method="spearman")
qplot(a.stat,b.stat)

