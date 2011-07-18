#Load rJava
setwd("~/Dropbox/jlucrum/rexperimental/")
source("preload.r")

rluc.preload("/home/house/NetBeansProjects/JLucrum/dist/lib",
              "/home/house/NetBeansProjects/JLucrum/build/classes",
              "~/Dropbox/jlucrum/rexperimental/");


#Reference: http://www.r-bloggers.com/arma-models-for-trading-part-ii/
jluc.bestarima <- function(x.ts, perm=c(0,0,0,3,3,3), method="ML", trace=F) {
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

      fit = tryCatch( arima(x.ts, order=c(p,d,q), method=method),
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

  print(paste("Best:",list(best.model), best.aic, method, sep=","))
  return(arima(x.ts, order=best.model, method=method))
}

DataFetcher <- J("org.jtotus.database.DataFetcher");
fetcher = new(DataFetcher);

metsoc <- fetcher$fetchPeriodByString("Metso Oyj", "01-01-2010", "01-06-2011", "CLOSE")
mets.ts <- ts(metsoc, frequency=1)
  
mets.ar <- jluc.bestarima(mets.ts, perm=c(0,0,0,2,2,2), method='CSS')
mets.perd <- predict(mets.ar, n.ahead=3)
plot(mets.ts, type="l")
lines(mets.perd$pred, col="blue")
