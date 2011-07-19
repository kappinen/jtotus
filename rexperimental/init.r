setwd("~/Dropbox/jlucrum/rexperimental/");
source("preload.r", local=F)
rluc.preload("/home/house/NetBeansProjects/JLucrum/dist/lib",
              "/home/house/NetBeansProjects/JLucrum/build/classes",
              "~/Dropbox/jlucrum/rexperimental/");
stockNames
#profiling example, system.time(source("preload.r"))


DataFetcher <- J("org.jtotus.database.DataFetcher");
fetcher = new(DataFetcher);


metsov <- fetcher$fetchPeriodByString("Metso Oyj", "01-01-2008", "30-6-2011", "VOLUME");

nesteoil <- fetcher$fetchPeriodByString("Neste Oyj", "01-01-2008", "30-6-2011", "CLOSE");
len = length(metsoc) - 3;
mets <- metsoc[0:len];

plot(metsoc, type="l")
metsoc.diff <- diff(metsoc);
metsov.diff <- diff(metsov);





manipulate(
  # plot expression
  plot(rnorm(10000), xlim = c(y.max, x.max), ylim = c(y.min,x.min),type = type, ann = label),
  # controls
  x.max = slider(0, 1000, step = 10, initial = 1000),
  y.max = slider(0, 1000, step = 10, initial = 0),
  y.min = slider(0, 1000, step = 10, initial = 0),
  x.min = slider(0, 1000, step = 10, initial = 0),
  type = picker("Points" = "p", "Line" = "l", "Step" = "s"),
  label = checkbox(TRUE, "Draw Labels")
)

test2 <- rnorm(1000);
test3 <- rnorm(1000);
plot(test2,test3)
plot(density(test2))

#usd/euro
EXUSEU <- getSymbols("EXUSEU",src="FRED", from="2000-01-01")
#gold XAU, silver XAG
getMetals("XAU", from=Sys.Date()-50)
getMetals("XAG", from=Sys.Date()-50)

plot(XAUUSD)
getSymbols("MCOILBRENTEU", src="FRED", from="2011-01-01")
chartSeries(MCOILBRENTEU, theme="white")
addBBands()


gtools::running(nesteoil, metsoc, fun=corr)
plot(gtools::running(nesteoil, metsoc, fun=cor, width=10), type="l")
ccf(diff(log(nesteoil)), diff(log(metsoc)))
nesteoil <- fetcher$fetchPeriodByString("Neste Oil", "01-01-2011", "19-7-2011", "CLOSE");
metsoc <- fetcher$fetchPeriodByString("Metso Oyj", "01-01-2011", "19-7-2011", "VOLUME");

getSymbols("XPT/USD",src="oanda",  from="2011-01-01")
EURUSD<-getPrice(to.monthly(getSymbols("EURUSD=X",auto.assign=FALSE),indexAt='lastof',drop.time=TRUE))
EURUSD<-getPrice(to.monthly(getSymbols("EXUSEU",src="FRED", from="2000-01-01"),indexAt='lastof',drop.time=TRUE))
Cl(EXUSEU)
GSPC.rets = diff(log(Cl(GSPC)))








#! A-shares have no voting rights !

#
#
# ts.metsoc = ts(metsoc, start=1, frequency=12)
# plot(stl(ts.metsoc, s.window="periodic"))
#plot(ukgas[53:106] - stl(ukgas, s.window="per")$time.series[, "trend"][53:106], type="l", col="blue")
#lines(ukgas[0:53] - stl(ukgas, s.window="per")$time.series[, "trend"][0:53], col="red")
 
# http://www.bloomberg.com/apps/quote?ticker=GSPG10YR:IND
# http://www.bloomberg.com/apps/quote?ticker=GBTPGR30:IND


#http://www.r-bloggers.com/millionaire%E2%80%99s-advice/
#moving correlation

X <- rnorm(1000); Y <- rnorm(1000)
plot(running(X, Y, cor))
plot(X, Y)
plot(running(X, Y, fun=cor, width=100), type="l")

#lagged correlation
ccf(X,Y)


# IBrokers
# http://www.r-bloggers.com/algorithmic-trading-with-ibrokers/


require(quantmod)

Sys.setenv(TZ="GMT")
getSymbols('SPY',from='2000-01-01')
x=data.frame(d=index(Cl(SPY)),return=as.numeric(Delt(Cl(SPY))))
ggplot(x,aes(return))+stat_density(colour="steelblue", size=2, fill=NA)+xlab(label='Daily returns')

require(zoo)
?rapply


#hashmap like ->
> x <- rnorm(4)
> names(x) <- c("a", "b", "c", "d")
> x
         a          b          c          d
-1.4122868  1.3588267 -0.5499391 -0.3581889
> x["d"]
         d
-0.3581889

#http://www.investuotojas.eu/
#http://www.quanttrader.info/public/
