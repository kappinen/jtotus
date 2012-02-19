jluc.testModel1 <- function(asset="TESTcompany",
                            fromDate=as.Date(Sys.Date()-150),
                            toDate=Sys.Date(), 
                            lag=1)
{
  
  start<-as.double(difftime(toDate, fromDate, unit="days"))
  
  new.data<-NULL
  for (i in start:0) {
    new.date <- toDate - i
    new.data <-rbind(new.data, xts(as.numeric(new.date) - 10000 , new.date))
  }
  
  #--------------------------------------------------------
  targetPrice<-cos(new.data) + 3;
  
  dailyReturn<-jluc.detrend(log(targetPrice), n=1)
  reg<-na.omit(lag(targetPrice, k=lag))
  
  step<-jluc.detrend(log(lag(targetPrice, k=lag)), n=1, name="step")
  step2<-jluc.detrend(log(lag(targetPrice, k=lag+1)), n=1, name="step2")
  step3<-jluc.detrend(log(lag(targetPrice, k=lag+2)),n=1, name="step3")
  
  model<-na.omit(merge(dailyReturn,step,step2,step3))
  
  index <- rowSums(!is.finite(model)) >= 1 
  pmodel <- model[!index, ]
  smodel<-na.omit(scale(pmodel))
  
  if (length(smodel) != 0) {
    names(smodel)<-c("target", "step", "step2", "step3")
  } else {
    return(NULL);
  }
  
  return(smodel)
}