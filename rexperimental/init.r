rluc.preload("/home/house/NetBeansProjects/JLucrum/dist/lib",
              "/home/house/NetBeansProjects/JLucrum/build/classes",
              "~/Dropbox/jlucrum/rexperimental/");


DataFetcher <- J("org.jtotus.database.DataFetcher");
fetcher = new(DataFetcher);


metsov <- fetcher$fetchPeriodByString("Metso Oyj", "01-01-2008", "30-6-2011", "VOLUME");

metsoc <- fetcher$fetchPeriodByString("Metso Oyj", "01-01-2008", "30-6-2011", "CLOSE");
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


EXUSEU <- getSymbols("EXUSEU",src="FRED", from="2000-01-01")

EURUSD<-getPrice(to.monthly(getSymbols("EURUSD=X",auto.assign=FALSE),indexAt='lastof',drop.time=TRUE))
EURUSD<-getPrice(to.monthly(getSymbols("EXUSEU",src="FRED", from="2000-01-01"),indexAt='lastof',drop.time=TRUE))
Cl(EXUSEU)

getSymbols("XPT/USD",src="Oanda")
GSPC.rets = diff(log(Cl(GSPC)))

length(EURUSD)
plot(EXUSEU);









#
#
# ts.metsoc = ts(metsoc, start=1, frequency=12)
# plot(stl(ts.metsoc, s.window="periodic"))
#plot(ukgas[53:106] - stl(ukgas, s.window="per")$time.series[, "trend"][53:106], type="l", col="blue")
#lines(ukgas[0:53] - stl(ukgas, s.window="per")$time.series[, "trend"][0:53], col="red")
 
# http://www.bloomberg.com/apps/quote?ticker=GSPG10YR:IND
# http://www.bloomberg.com/apps/quote?ticker=GBTPGR30:IND







