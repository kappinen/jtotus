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

#
jlu.testStocks(names="Outotec Oyj", src="jtotus");



# Total diff: 1.09782627946778  length: 46

print("-----Running simple--------")
jlu.testStocks(names="MXCYY", src="yahoo");

print("Running lowest only")
xreg <- as.ts(Lo(MXCYY))
jlu.testStocks(names="MXCYY", src="yahoo", xreg=xreg);

print("Running hiest only")
xreg <- as.ts(Hi(MXCYY))
jlu.testStocks(names="MXCYY", src="yahoo", xreg=xreg);

print("Running low only diff log")
xreg <- as.ts(diff(log(Lo(MXCYY))))
jlu.testStocks(names="MXCYY", src="yahoo", xreg=xreg);

print("Running low only diff ")
xreg <- as.ts(diff(Lo(MXCYY)))
jlu.testStocks(names="MXCYY", src="yahoo", xreg=xreg);

print("Running hiest only diff log")
xreg <- as.ts(diff(log(Hi(MXCYY))))
jlu.testStocks(names="MXCYY", src="yahoo", xreg=xreg);

print("Running hiest only diff ")
xreg <- as.ts(diff(Hi(MXCYY)))
jlu.testStocks(names="MXCYY", src="yahoo", xreg=xreg);
??gnm


xreg <- as.ts(diff(Lo(get(name))))

      #xreg2 <- as.ts(diff(Lo(get(name))))
      #xreg <- cbind(xreg1, xreg2)
      #xreg <- as.ts(Hi(get(name)))



xreg1 <- as.ts(diff(Hi(MXCYY)))
xreg2 <- as.ts(diff(Lo(MXCYY)))
voo <- cbind(xreg1,xreg2)

voo[1:10, ]

plot(c(0:100), main="some")

vo <- "test"
test <- rbind(test, vo, 1)
rbind(testii, data.frame(test="ibo2", vol=2))

stockData2 <- fetcher$fetchPeriodByString("Outotec Oyj", "01-01-2011", Sys.Date(),"CLOSE")

stockData <- fetcher$fetchPeriod("Outotec Oyj", "01-01-2011","01-08-2011", "CLOSE")
vo <- fetcher$fetchPeriodByString("Pohjola Bank A", "30-07-2011","01-01-2011", "CLOSE")
vo <- fetcher$fetchPeriodByString("Pohjola Bank A", "01-01-2011","30-07-2011", "CLOSE")


#GLM example
length(stockData)
length(t)
t <- c(1:145)
fitted <- glm(stockData~t)
fitted$coefficients
lines(-0.03212829*t+42.90833046, col="red")
plot(y=stockData,x=t, type="l")


lines(filter(stockData, filter=rep(1/20,20),sides=1), col="red")
lines(filter(stockData, filter=rep(1/5,5),sides=1), col="blue")
plot(stockData, type="l")


jlu.testStocks(names=c("Pohjola Bank A"), src="jtotus");
DataFetcher <- J("org.jtotus.database.DataFetcher");

jlu.testStocks(names=c("Metso Oyj"), src="jtotus");
jlu.testStocks(stockNames[1:5], src="jtotus");

DataFetcher <- J("org.jtotus.database.DataFetcher");
fetcher = new(DataFetcher);

metsoc <- fetcher$fetchPeriod("Metso Oyj", "01-01-2010", "01-06-2011", "CLOSE")
mets.ts <- ts(metsoc, frequency=1)

mets.perd <- predict(mets.ar, n.ahead=3)
plot(mets.ts, type="l")
lines(mets.perd$pred, col="blue")

jlu.testStocks(names=c("Metso Oyj"), src="jtotus");

#http://www.r-bloggers.com/arma-models-for-trading-part-iii/
library(fGarch)

SPY.rets = diff(log(Cl(MXCYY)))

glm()
plot(SPY.rets)
jluc.bestarima(x.ts=)

voi <- as.ts(Cl(MXCYY))
library(fGarch)
library(outliers)
Cl(I(testii))
getSymbols("MXCYY", from="2009-01-01", to=Sys.Date()-5)
data <- Cl(MXCYY)
vol <- Vo(MXCYY)

vol[vol == 0] = 1
data.a <- diff(log2(data))
vol.a <- diff(log2(vol))

plot(density(na.trim(vol.a)))
source(url("http://www.stat.pitt.edu/stoffer/tsa2/Rcode/itall.R")) 
lag.plot2(as.ts(a),as.ts(b),max.lag=5 ,smooth=T)
a<-na.trim(data.a)
b<-na.trim(vol.a)


lag.plot(as.ts(a),as.ts(b),lags=5)
pacf(as.ts(na.trim(vol.a)), lag.max=20, )
plot(as.ts(data.a),as.ts(vol.a))
fitData.arima <- jluc.bestarima(data, perm=c(0,0,0,2,2,2), method='ML')
fitData.pred <- predict(fitData.arima, n.ahead=1)
predictionValue <- as.double(fitData.pred$pred)




aa<- outlier()
datats <- as.ts(data)
predictedValue <- jlu.testModel(datats, window=50, print=F, title="Metso Oyj")
predictedValue <- jlu.testModel(datats, window=80, print=F, title="Metso Oyj")
predictedValue <- jlu.testModel(datats, window=110, print=F, title="Metso Oyj")

??arma
getSymbols("SPY", from="2011-01-01", to="2011-07-5")
SPY.rets = diff(log(Cl(SPY)))
SPY.rets = Cl(SPY)
plot(log(fft(SPY.rets)))
mets.ar <- jluc.bestarima(SPY.rets, perm=c(0,0,0,2,2,2), method='CSS')
SPY.garch = garchFit(~arma(0, 1) + garch(1, 1), data=as.ts(tail(SPY.rets, 50)))
predict(mets.ar, n.ahead=1, doplot=F)
predict(SPY.garch, n.ahead=1, doplot=F)
