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
jluc.autoLag <- function(future, past, max.lag=3, plot=F) {
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
    abline(v=(seq(-max.lag,max.lag,1)), col="gray", lty="dotted")
    ccf(future, series, lag.max=max.lag)
    abline(v=(seq(-max.lag,max.lag,1)), col="gray", lty="dotted")
  }

  return(na.omit(lag(aa$r, k=best.lag)))
}

jluc.autoPickModel <- function(model, target_name="target", debug=F, plot=F) {
  newmodel<-jluc.autoModel(model,target_name, debug)
  if (plot) {
    variables <- names(model)
    tnames <- variables[variables != target_name]
    var_formula<-do.call("paste", c(as.list(tnames), sep = "+"))
    formula <- paste(target_name, var_formula, sep="~")
    mod.fitted<-glm(formula=formula, data=model)
    
    par(mfrow=c(1,1))
    plot.ts(as.ts(model$target))
    lines(fitted(mod.fitted), col="blue")
    lines(fitted(newmodel), col="red")
    lines(rep(0, times=length(model$target)), col="green")  
  }

  return(newmodel)
}
  

jluc.autoModel <- function(model, target_name="target", debug=F) {
  bestGOF <- 1000
  bestModel <- ""
  
  if (debug) { print(paste(names(model))) }
  variables <- names(model)
  for(ignore in 1:length(variables)) {
    
    variables <- names(model)
    if (variables[ignore]!=target_name) {
      newmodel <- model[,!(names(model) %in% variables[ignore])]
    } else {
      newmodel <- model
    }
    
    variables <- names(newmodel)
    tnames <- variables[variables != target_name]
    
    var_formula<-do.call("paste", c(as.list(tnames), sep = "+"))
    formula <- paste(target_name, var_formula, sep="~")
    
    if (debug) { print(formula) }
    
    mod.fitted<-glm(formula=formula, data=newmodel)

    #FIXME:Better Goodness of fit - criteria
    if (mod.fitted$aic < bestGOF) {
      bestGOF <- mod.fitted$aic
      bestModel <- mod.fitted
      }
  }
  
  #FIXME:Assumes that best model is found
  if (ncol(model) != ncol(bestModel$data)) {
    recModel <- Recall(bestModel$data, target_name, debug)
    if (recModel$aic < bestModel$aic) {
      bestModel <- recModel
    }  
  }
  
   return(bestModel);
}
