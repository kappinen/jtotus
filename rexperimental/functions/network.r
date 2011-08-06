jluc.fetch <- function(name, from=as.Date("2011-01-01"), to=Sys.Date(), src="jtotus") {

  if (!is.null(src) && src == "jtotus") {
      startDate <- format(from, '%d-%m-%Y')
      endDate <- format(to, '%d-%m-%Y')
      stockData <- fetcher$fetchPeriod(name, startDate, endDate, "CLOSE")
    } else {
      if (!is.null(src)) {
        newdata <- getSymbols(name, from=format(from), to=format(to), src=src)  
      } else {
        newdata <- getSymbols(name, from=format(from), to=format(to))
      }
      #stockData <- Cl(get(name))
      stockData <- get(newdata)
    }
  print(paste("Fetched:", length(stockData)))
  return(stockData)
}