#Converts Map to time series object
jluc.convMapToTs <- function(map) {
  
  simpleMap <- .jstrVal(map)
  simpleMap2<-gsub("^\\{", "", simpleMap)
  simpleMap3<-gsub("}$", "", simpleMap2)
  dayAndValue<-unlist(strsplit(simpleMap3, split=","))
  retTimeSeries<-NULL
  for (i in dayAndValue) {
    line<-unlist(strsplit(i,split="="))
    newvalue<-xts(as.double(line[2]), as.Date(line[1]))
    retTimeSeries<-rbind(retTimeSeries, newvalue)
  }
  
  return(retTimeSeries)

#  data <- data.frame(row.names=c("date", "value"))

#  entrySet <- map$entrySet()
#  iter<-entrySet$iterator()
#  while(.jsimplify(iter$hasNext())) {
#    nextEntry <- iter$"next"()
#    dayData<-data.frame(date=as.Date(nextEntry$getKey()), 
#                     value=nextEntry$getValue());
#    data <- rbind(data,dayData);
#  }
#  tsdata <- xts(data[,2], data[,1])
#  return(tsdata)
}


#wrapper for quantmod with jlucrum
jluc.fetch <- function(name, from=as.Date(Sys.Date()-252), to=Sys.Date(), src="jlucrum", type="close")
{
  if (!is.null(src) && src == "jlucrum") {
      tmpData <- fetcher$fetchPeriodData(name, format(from), format(to), type)
      stockValue<-jluc.convMapToTs(tmpData)
    } else {
      if (!is.null(src)) {
        newdata <- getSymbols(name, from=format(from), to=format(to), src=src)  
      } else {
        newdata <- getSymbols(name, from=format(from), to=format(to))
      }
      #stockData <- Cl(get(name))
      stockValue <- get(newdata)
    }

  if (length(stockValue) != 0) {
    colnames(stockValue) <- c(name)
  }

  return(stockValue)
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

jluc.detrend <- function(data, n=5, plot=F, name=NULL) {
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
    
    stationaryTest<-SuppressWarnings(adf.test(as.double(final)))
    dvalue=paste("DF",round(stationaryTest$statistic, digits=4), sep="=")
    p2value=paste("p",round(stationaryTest$p.value, digits=4), sep="=")
    statMesg=paste(dvalue, p2value, sep=" ")
    
    legend("topleft", legend=shapiroMesg, text.col ="blue", bg="white", x.intersp=0)
    legend("bottomright", legend=statMesg, text.col ="blue", bg="white", x.intersp=0)
  }
  
  if (!is.null(name)) {
    names(final) <- c(name)
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

  max.lag <- min(ifelse(length(future) - 1 >= 0,
                        length(future) - 1,
                        Inf),
                 ifelse(length(past) - 1 >= 0,
                        length(past) - 1,
                        Inf),
                        max.lag)
  
  tseries<-as.double(aa$t)
  rseries<-as.double(aa$r)
  
  aa.ccf <- ccf(tseries, rseries, lag.max=max.lag, plot=plot)
  aa.abs<-abs(aa.ccf$acf[I(max.lag+1):I(2*max.lag+1)])
  best.lag<-max(0, which.max(aa.abs)-1)

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

  na.omit(lag(aa$r, k=best.lag))
  
  return(na.omit(lag(aa$r, k=best.lag)))
}

jluc.autoPickModel <- function(model, target_name="target", debug=F, plot=F) {
  newmodel<-jluc.autoModel(model=model,target_name=target_name, debug=debug)
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
    legend("topleft", legend=paste(mod.fitted$formula, mod.fitted$aic,sep="="), text.col ="blue", bg="white", x.intersp=0)
    legend("bottomleft", legend=paste(newmodel$formula,newmodel$aic,sep="="), text.col ="red", bg="white", x.intersp=0)
    lines(rep(0, times=length(model$target)), col="green")  
  }

  return(newmodel)
}

jluc.autoModel <- function(model, target_name="target", fitfunc="glm", debug=F, maxsize=200) {

  variables <- names(model)
  tnames <- variables[variables != target_name]
  var_formula<-do.call("paste", c(as.list(tnames), sep = "+"))
  formula <- paste(target_name, var_formula, sep="~")  

  newformula<-do.call(what="glmulti", args=list(
                      y=formula,data=model,
                      method="g", crit="aicc", 
                      plotty=F, report=F,
                      confsetsize=20, minsize=4, popsize=7,
                      fitfunction=toString(fitfunc)))

  call.formula<-summary(newformula)$bestmodel
  
  bestModel<-do.call(what=toString(fitfunc), args=list(formula=call.formula, data=model))
  cat("Best selected model:", call.formula, " aic:", bestModel$aic, "\n");
  return(bestModel);
}

#Depricated / replaced by glmulti
# jluc.autoModel <- function(model, target_name="target", debug=F) {
#   bestGOF <- 1000
#   bestModel <- ""
#   
#     if (debug) { 
#       print(paste(names(model))) 
#     }
#     
#     variables <- names(model)
#     for(ignore in 1:length(variables)) {
# 
#       variables <- names(model)
#       if (variables[ignore]!=target_name) {
#         newmodel <- model[,!(names(model) %in% variables[ignore])]
#       } else {
#         newmodel <- model
#       }
# 
#       if (ncol(newmodel) == 1) {
#         next;
#       }
# 
#       variables <- names(newmodel)
#       tnames <- variables[variables != target_name]
#     
#       var_formula<-do.call("paste", c(as.list(tnames), sep = "+"))
#       formula <- paste(target_name, var_formula, sep="~")
#     
#       if (debug) { print(formula) }
#     
#       mod.fitted<-glm(formula=formula, data=newmodel)
# 
#       #FIXME:Better Goodness of fit - criteria
#       if (mod.fitted$aic < bestGOF) {
#         if (debug) { print(paste("Best aic", mod.fitted$aic)) }
#         bestGOF <- mod.fitted$aic
#         bestModel <- mod.fitted
#         }
#     }
#   
#   #FIXME:Assumes that best model is found
#   if (ncol(model) != ncol(bestModel$data)) {
#     recModel <- Recall(bestModel$data, target_name, debug)
#     if (recModel$aic < bestModel$aic) {
#       bestModel <- recModel
#     }
#   }
#   
#    return(bestModel);
# }


jluc.modelComparePlot <- function(model, formula, newformula) {

  mod.fitted<-glm(formula=formula, data=model)
  newmodel<-glm(formula=newformula, data=model)

  par(mfrow=c(1,1))
  plot.ts(as.ts(model$target))
  lines(fitted(mod.fitted), col="blue")
  lines(fitted(newmodel), col="red")
  legend("topleft", legend=paste(mod.fitted$formula, mod.fitted$aic,sep="="), text.col ="blue", bg="white", x.intersp=0)
  legend("bottomleft", legend=paste(newmodel$formula,newmodel$aic,sep="="), text.col ="red", bg="white", x.intersp=0)
  lines(rep(0, times=length(model$target)), col="green")  
  
  return(newmodel)
}

jluc.predict <- function(stockName = "Metso Oyj",
                         fromDate=as.Date(Sys.Date()-150),
                         toDate=Sys.Date(),
                         modelfunc="jluc.createModel", debug=F) {
  prediction<-NULL
  ############# [DATA | FIT] #################
  
  if (debug) {
    cat("Using model:", modelfunc,"\n")
  }
  
  model<-do.call(what=toString(modelfunc), args=list(
    asset=toString(stockName),
    fromDate=fromDate, toDate=toDate, lag=1))

  mod.fitted<-jluc.autoPickModel(model, "target", plot=F)
  
  ############# [DATA | APPLY] #################
  #Fetch new data for asset
  newdata<-do.call(what=toString(modelfunc), args=list(
    asset=toString(stockName),
    fromDate=toDate-15, toDate=toDate, lag=0))
  
  if (debug) {
    print(paste("From date:", fromDate))
    print(paste("To date:", toDate))
    print(paste("newdata",last(newdata)))
    print(paste("model", last(model)))
    print(paste("formula:", mod.fitted$formula))
  }
  
  if (!is.null(newdata)) {
    newvalues <- last(newdata)
    prediction<-predict(object=mod.fitted, newdata=newvalues, n.ahead=1)
  }
  
  return(prediction)
}
