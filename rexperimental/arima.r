#Reference: http://www.r-bloggers.com/arma-models-for-trading-part-ii/
jluc.bestarima <- function(x.ts, perm=c(0,0,0,3,3,3), method="ML", trace=F, xreg=NULL) {
  best.aic <- 1e9
  n <- length(x.ts)

  if (method != "CSS" & method != "ML") {
      print(paste("Method ", method, " not supported"))
      return(FALSE)
  }

  for(p in perm[1]:perm[4]) for(d in perm[2]:perm[5]) for(q in perm[3]:perm[6]){
    if( p == 0 && q == 0 )
      {
         next
      }

      fit = tryCatch( arima(x.ts, order=c(p,d,q), method=method, xreg=xreg),
                      error=function( err ) FALSE,
                      warning=function( warn ) FALSE )

      if( !is.logical( fit ) ) {
        if( method == 'CSS' ) {
          fit.aic <- -2 * fit$loglik + (log(n) + 1) * length(fit$coef)
        } else {
          fit.aic <- fit$aic
        }

        if (trace) {
          print(paste(p,d,q, fit$aic, fit.aic))
        }

        if(fit.aic < best.aic)
        {
          best.aic <- fit.aic
          best.fit <- fit
          best.model <- c(p,d,q)
        } 
      } else {
        if (trace) {
          print(paste(p,d,q, "none"))  
        }
      }   
  }

  #print(paste("Best:",list(best.model), best.aic, method, sep=","))
  return(arima(x.ts, order=best.model, method=method))
}


#testModel
jlu.testModel <- function(fitData=NULL, window=100, print=F, xreg=NULL, plot=T, title="Test") {
  allPred <- c()
  extReg <- NULL

  if(!is.ts(fitData)) {
    fitData <- as.ts(data=fitData)
  }

  totalLen <- length(fitData) - 1

  if (print) {
    print(paste("len is :", totalLen, " and window:", window))  
  }
  
  #plot against market data
  if (plot) {
      for ( i in window:totalLen) {
        dataForFit <- fitData[I(i-window):i]
        if (!is.null(xreg)) {
          extReg <- xreg[I(i-window):i];
          #extReg <- xreg[I(i-window):i, ];
        }

        fitData.arima <- jluc.bestarima(dataForFit, perm=c(0,0,0,2,2,2), method='ML', xreg=extReg)
        #fitData.arima <- jluc.bestarima(dataForFit, perm=c(0,0,0,2,2,2), method='ML', xreg=volData)
        fitData.pred <- predict(fitData.arima, n.ahead=1)
        predictionValue <- as.double(fitData.pred$pred)

        
        if (print) {
          print(paste("Market Value:", fitData[I(i+1)], " Headed" , fitData[I(i+1)] - fitData[i]))
          print(paste("Predicted Value:", predictionValue, " Headed" , predictionValue - fitData[i]))
          print(paste("diff(market-pred)", fitData[I(i+1)] - predictionValue))
        }

        #predictionValue - fitData[i];

        allPred <- rbind(allPred, predictionValue, deparse.level=0)
    }
    
    #plot(diff(fitData[I(window + 1):I(totalLen + 1)]), type="l", main=title)
    #lines(diff(allPred), col="red")  
      residual = fitData[I(window + 1):I(totalLen + 1)] - allPred[ , 1]
      plot(residual, type="l", main=paste(title, " residual"))
      print(paste("Total diff:", I(sum(abs(residual)) / length(residual)) , " length:", length(residual)))
  }

  #Predicting next value
  predLend <- length(fitData)
  dataForFit <- fitData[I(predLend-window):predLend]

  fitData.arima <- jluc.bestarima(dataForFit, perm=c(0,0,0,2,2,2), method='ML', xreg=extReg)
  fitData.pred <- predict(fitData.arima, n.ahead=1)
  predictedValue <- as.double(fitData.pred$pred)

  return(predictedValue);
}

#testStocks
jlu.testStocks <- function(names=NULL, from=as.Date("2011-01-01"), to=Sys.Date(), window=100, print=F, src="jtotus", xreg=NULL) {
  print(paste("from:", format(from), " to:", format(to)))
  for(name in names) {
    if (src == "jtotus") {
      startDate <- format(from, '%d-%m-%Y')
      endDate <- format(to, '%d-%m-%Y')
      #stockData <- fetcher$fetchPeriodByString(name, endDate,startDate, "CLOSE")
      stockData <- fetcher$fetchPeriod(name, startDate, endDate, "CLOSE")
    } else {
      name.data <- getSymbols(name, from=format(from), to=format(to))
      stockData <- Cl(get(name))
      #xreg <- as.ts(diff(log(Lo(get(name)))))
      #xreg2 <- as.ts(diff(log(Lo(get(name)))))
      #xreg <- cbind(xreg1, xreg2)
      #xreg <- as.ts(Hi(get(name)))
    }
    fitData <- as.ts(stockData)
    
    predictedValue <- jlu.testModel(fitData, window=window, print=print, xreg=xreg, title=name)
    
    print(paste(name, " predicted:", predictedValue, " Current", last(fitData), "Diff:", predictedValue- last(fitData)))
  }
}

