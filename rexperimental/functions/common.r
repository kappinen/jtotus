#Converts Map to time series object
jluc.convMapToTs <- function(map) {
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
jluc.fetch <- function(name, from=as.Date(Sys.Date()-252), to=Sys.Date(), src="jlucrum")
{
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
  colnames(stockData) <- c(name)
  return(stockData)
}

# Volatility
# http://en.wikipedia.org/wiki/Volatility_(finance)
jluc.volatility<-function(data, period=-1, norm=F) {
  if (period > 0) {
    len <- period
  } else {
    len <- length(data)
  }
  
  if (norm) {
    volatility <- sd(na.omit(diff(log(data))))*sqrt(1/len)
  }else {
    volatility <- sd(data)*sqrt(1/len)  
  }
  
  return(volatility)
}
