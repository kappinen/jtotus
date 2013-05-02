agrdayOfWeek<-function(target) {
  target<-jluc.fetch("Metso Oyj", from=Sys.Date()-250, type="close");
  dtarget<-diff(target)
  dates <- as.POSIXct(index(dtarget), format = "%Y-%m-%d")
  dayLookup <- 1:7
  names(dayLookup) <- c("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  datesDays <- dayLookup[weekdays(dates, abbreviate = TRUE)]
  void<-xts(na.omit(cbind(dtarget, dow=datesDays), order.by = dates))

  table<-rep(0, times=7)
  for(i in 1:dim(void)[1]) {
    if (void[i,1]>0) {
      table[as.integer(void[i,2])] <- table[as.integer(void[i,2])] + 1
    } else {
      table[as.integer(void[i,2])] <- table[as.integer(void[i,2])] - 1
    }
  }
}

