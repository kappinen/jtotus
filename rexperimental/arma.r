jluc.bestarma <- function(x.ts, perm=c(0,0,0,3,3,3), method="ML", trace=F, xreg=NULL) {
  best.aic <- 1e9
  n <- length(x.ts)

  if (method != "CSS" & method != "ML") {
      print(paste("Method ", method, " not supported"))
      return(FALSE)
  }

  for(p in perm[1]:perm[4]) for(q in perm[3]:perm[6]){
    if( p == 0 && q == 0 )
      {
         next
      }

      fit = tryCatch( arima(x.ts, order=c(p,q), method=method, xreg=xreg),
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


