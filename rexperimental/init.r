rluc.preload("/home/house/NetBeansProjects/JLucrum/dist/lib",
              "/home/house/NetBeansProjects/JLucrum/build/classes",
              "~/Dropbox/jlucrum/rengine");













plot(test2)

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

library(quantmod)
getSymbols( "EURUSD=X", from="2000-01-01" )
getSymbols("EXUSEU",src="FRED")
EURUSD<-getPrice(to.monthly(getSymbols("EURUSD=X",auto.assign=FALSE),indexAt='lastof',drop.time=TRUE))
Cl(EURUSD)
getSymbols("XPT/USD",src="Oanda")
GSPC.rets = diff(log(Cl(GSPC)))

length(EURUSD)
plot(EXUSEU);








metsoc <- fetcher$fetchPeriodByString("Metso Oyj", "01-01-2008", "30-6-2011", "CLOSE");
metsov <- fetcher$fetchPeriodByString("Metso Oyj", "01-01-2008", "30-6-2011", "VOLUME");
metsoc.diff <- diff(metsoc);
metsov.diff <- diff(metsov);

#
#
# ts.metsoc = ts(metsoc, start=1, frequency=12)
# plot(stl(ts.metsoc, s.window="periodic"))
#plot(ukgas[53:106] - stl(ukgas, s.window="per")$time.series[, "trend"][53:106], type="l", col="blue")
#lines(ukgas[0:53] - stl(ukgas, s.window="per")$time.series[, "trend"][0:53], col="red")
 







.jinit(classpath="/home/house/NetBeansProjects/jtotus/build/classes")
.jaddClassPath("/home/house/jtotuslibs/joda-time-1.6.2.jar")
DataFetcher <- J("org.jtotus.database.DataFetcher");
fetcher = new(DataFetcher);







