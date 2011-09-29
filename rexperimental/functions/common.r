#Converts Map to time series object
rluc.convMapToTs <- function(map) {
  data <- data.frame(row.names=c("date", "value"))

  entrySet <- map$entrySet()
  iter<-entrySet$iterator()
  while(.jsimplify(iter$hasNext())) {
    nextEntry <- iter$"next"()
    dayData<-data.frame(date=as.Date(nextEntry$getKey()), 
                     value=nextEntry$getValue());
    data <- rbind(data,dayData);
  }
  
  tsdata <- xts(data[,2], data[,1])
  return(tsdata)
}

#wrapper for quantmod with jlucrum
jluc.fetch <- function(name, from=as.Date("2011-01-01"), to=Sys.Date(), src="jtotus") {

  if (!is.null(src) && src == "jlucrum") {
      tmpData <- fetcher$fetchPeriodData(name, format(from), format(to), "close")
      stockData<-rluc.convMapToTs(tmpData)
    } else {
      if (!is.null(src)) {
        newdata <- getSymbols(name, from=format(from), to=format(to), src=src)  
      } else {
        newdata <- getSymbols(name, from=format(from), to=format(to))
      }
      #stockData <- Cl(get(name))
      stockData <- get(newdata)
    }
  print(paste("Fetched:", length(stockData)))
  return(stockData)
}