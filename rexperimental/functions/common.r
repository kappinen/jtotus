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
jluc.fetch <- function(name, from=as.Date(Sys.Date()-252), to=Sys.Date(), src="jlucrum", type="close")
{
  if (!is.null(src) && src == "jlucrum") {
      tmpData <- fetcher$fetchPeriodData(name, format(from), format(to), type)
      stockData<-jluc.convMapToTs(tmpData)
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


# no unit-root -> stationary process
# contains a unit-root -> non-stationary

jluc.detrend <- function(data, n=5, plot=T) {
  # shapiro.qqnorm
  if (n==1) {
    final <- na.omit(diff(data))
  }else {
    sma <- SMA(na.omit(data[!is.infinite(data)]), n)
    merged<-merge(data, sma)
    names(merged) <- c("data", "sma")
    final <- na.omit(merged$data-merged$sma)
  }
  
  if (plot) {
    qqnorm(final)
    qqline(final)
    normTest<-shapiro.test(as.double(final))
    wvalue=paste("W",round(normTest$statistic, digits=4), sep="=")
    pvalue=paste("p",round(normTest$p.value, digits=4), sep="=")
    shapiroMesg=paste(wvalue, pvalue, sep=" ")
    
    stationaryTest<-adf.test(as.double(final))
    dvalue=paste("DF",round(stationaryTest$statistic, digits=4), sep="=")
    p2value=paste("p",round(stationaryTest$p.value, digits=4), sep="=")
    statMesg=paste(dvalue, p2value, sep=" ")
    
    legend("topleft", legend=shapiroMesg, text.col ="blue", bg="white", x.intersp=0)
    legend("bottomright", legend=statMesg, text.col ="blue", bg="white", x.intersp=0)
  }
  return(final)
}
