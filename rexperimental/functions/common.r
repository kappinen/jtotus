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
  if (n == 0) {
    final <- na.omit(data)
  } else if ( n==1 ) {
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

#Converts to time series and plot
jluc.plag2 <- function(a,b) {
  merged<-na.omit(merge(a,b))
  names(merged) <- c("a", "b")
  a_series <- as.ts(merged$a)
  b_series <- as.ts(merged$b)
  lag.plot2(a_series, b_series, max.lag=5, smooth=T)
}

#Attempts to find best lag for time series
jluc.autoLag <- function(future, past, max.lag=5, plot=F) {
  aa<-na.omit(merge(future,past))
  names(aa)<-c("t","r")
  
  if (plot) {
    par(mfrow=c(2,1))
  }
  
  tseries<-as.double(aa$t)
  rseries<-as.double(aa$r)
  aa.ccf <- ccf(tseries,rseries, lag.max=max.lag, plot=plot)
  aa.abs<-abs(aa.ccf$acf[I(max.lag+1):I(2*max.lag+1)])
  best.lag<-which.max(aa.abs)-1
  maximum<-aa.ccf$acf[max.lag+best.lag+1]
  
  print(paste("best lag:", best.lag, " acf:", maximum))
  if (plot) {
    lagged<-lag(aa$r, k=best.lag)
    result<-na.omit(merge(future,lagged));
    names(result) <- c("t","r")
    future<-as.double(result$t)
    series<-as.double(result$r)
    ccf(future, series)
  }

  return(na.omit(lag(aa$r, k=best.lag)))
}
