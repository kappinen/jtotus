----
#Load rJava
# Interesting functions: na.locf
#For debugging :traceback()
#MA example filter(stockData, filter=rep(1/5,5),sides=1)
# http://blog.fosstrading.com/.

setwd("~/Dropbox/jlucrum/rexperimental/")
source("preload.r")
source("arima.r")
 
rluc.preload("/home/house/NetBeansProjects/JLucrum/dist/lib",
              "/home/house/NetBeansProjects/JLucrum/build/classes",
              "~/Dropbox/jlucrum/rexperimental/");


jlu.testStocks(names=stockNames, src="jtotus");

plot(c(0:100), main="some")

vo <- "test"
test <- rbind(test, vo, 1)
rbind(testii, data.frame(test="ibo2", vol=2))

stockData2 <- fetcher$fetchPeriodByString("Pohjola Bank A", "01-01-2011", "30-07-2011","CLOSE")

stockData <- fetcher$fetchPeriod("Metso Oyj", "01-01-2011","30-07-2011", "CLOSE")
vo <- fetcher$fetchPeriodByString("Pohjola Bank A", "30-07-2011","01-01-2011", "CLOSE")
vo <- fetcher$fetchPeriodByString("Pohjola Bank A", "01-01-2011","30-07-2011", "CLOSE")


lines(filter(stockData, filter=rep(1/20,20),sides=1), col="red")
lines(filter(stockData, filter=rep(1/5,5),sides=1), col="blue")
plot(stockData, type="l")






jlu.testStocks(names=c("Pohjola Bank A"), src="jtotus");
DataFetcher <- J("org.jtotus.database.DataFetcher");



jlu.testStocks(names=c("Metso Oyj"), src="jtotus");
jlu.testStocks(stockNames[1:5], src="jtotus");

jlu.test <- function(start=as.Date("2011-01-01"), end=Sys.Date(), window=100, print=F) {
  startDate <- format(start)
  endDate <- format(end)
  getSymbols("MXCYY", from=startDate, to=endDate)
  stockData <- Cl(MXCYY)
  
  volData <- as.ts(Vo(MXCYY))
  fitData <- as.ts(stockData)

  totalLen <- length(fitData) - 1
  allPred <- c()

  for ( i in window:totalLen) {
    dataForFit <- fitData[I(i-window):i]
    fitData.arima <- jluc.bestarima(dataForFit, perm=c(0,0,0,2,2,2), method='ML')
    #fitData.arima <- jluc.bestarima(dataForFit, perm=c(0,0,0,2,2,2), method='ML', xreg=volData)
    fitData.pred <- predict(fitData.arima, n.ahead=1)
    predictionValue <- as.double(fitData.pred$pred)  

    if (print) {
      print(paste("Market Value:", fitData[I(i+1)], " Headed" , fitData[I(i+1)] - fitData[i]))
      print(paste("Predicted Value:", predictionValue, " Headed" , predictionValue - fitData[i]))
      print(paste("diff(market-pred)", fitData[I(i+1)] - predictionValue))
    }

    allPred <- rbind(allPred, predictionValue)
  }

  #plot against market data
  plot(diff(fitData[window:totalLen]), type="l")
  lines(diff(allPred), col="red")
  
  diffAll <- fitData[window:totalLen] - allPred
  print(paste("Total diff:", I(sum(abs(diffAll)^2) / length(diffAll)) , " length:", length(diffAll)))
}


DataFetcher <- J("org.jtotus.database.DataFetcher");
fetcher = new(DataFetcher);

metsoc <- fetcher$fetchPeriod("Metso Oyj", "01-01-2010", "01-06-2011", "CLOSE")
mets.ts <- ts(metsoc, frequency=1)


mets.perd <- predict(mets.ar, n.ahead=3)
plot(mets.ts, type="l")
lines(mets.perd$pred, col="blue")


#http://www.r-bloggers.com/arma-models-for-trading-part-iii/
library(fGarch)

SPY.rets = diff(log(Cl(MXCYY)))


plot(SPY.rets)
jluc.bestarima(x.ts=)

voi <- as.ts(Cl(MXCYY))
library(fGarch)
Cl(I(testii))

testii <- getSymbols("MXCYY", from="2011-01-01", to="2011-07-5")

ls()

??arma
getSymbols("SPY", from="2011-01-01", to="2011-07-5")
SPY.rets = diff(log(Cl(SPY)))
SPY.rets = Cl(SPY)
plot(log(fft(SPY.rets)))
mets.ar <- jluc.bestarima(SPY.rets, perm=c(0,0,0,2,2,2), method='CSS')
SPY.garch = garchFit(~arma(0, 1) + garch(1, 1), data=as.ts(tail(SPY.rets, 50)))
predict(mets.ar, n.ahead=1, doplot=F)
predict(SPY.garch, n.ahead=1, doplot=F)
