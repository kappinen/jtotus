#Plots moving-correlation and crosscorrelation by list
jluc.corrList <- function(listOfStocks) {

  breakFlag <- FALSE
  for (i in listOfStocks) for(y in listOfStocks) {
    if (i == y || breakFlag == T) {
      next
    }
    
    a<-jluc.fetch(i)
    b<-jluc.fetch(y)
    a.stat <- diff(log(a));
    b.stat <- diff(log(b));
  
    par(mfrow=c(2, 1))
    plot(gtools::running(a.stat, b.stat, fun=cor), type="l", xlab=paste(i,y, sep="-"))
    ccf(a.stat, b.stat)
    corr <- cor.test(a.stat, b.stat, method="spearman")
  
    print(paste("Done", i, y, " Cor:", corr$estimate))
    cmd <- readline()
    if (cmd == "q") {
      breakFlag <- TRUE
      break
    }
  }
}


#Plots moving-correlation and crosscorrelation
jluc.corr <- function(a, b, plot=T) {
    mergeresult <- merge.xts(a, b, join = "inner")
    mergeresult <- diff(log(mergeresult))
    merge.r <- mergeresult[!apply(is.na(mergeresult), 1, any),]
    names(merge.r) <- c("a", "b")  

    par(mfrow=c(2, 1))
    movingCorraltion <- gtools::running(merge.r$a, merge.r$b, fun=cor)
    plot(movingCorraltion, type="l", xlab=paste(deparse(substitute(a)),deparse(substitute(b)), sep="-"), ylab="Moving Correlation")
    ccf(as.ts(merge.r$a), as.ts(merge.r$b))

    corr <- cor.test(as.ts(merge.r$a), as.ts(merge.r$b), method="spearman")
    print(paste("Done Cor:", corr$estimate))

    return(corr$estimate)
}
