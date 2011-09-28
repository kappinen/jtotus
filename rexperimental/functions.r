rluc.convMapToTs <- function(map) {
  data <- data.frame(row.names=c("date", "value"))

  entrySet <- map$entrySet()
  iter<-entrySet$iterator()
  while(.jsimplify(iter$hasNext())) {
    nextEntry <- iter$"next"()
    dayData<-data.frame(date=as.Date(nextEntry$getKey()), 
                     value=nextEntry$getValue());
    data <- rbind(data,dayData);
  }
  
  tsdata <- xts(data[,2], data[,1])
  return(tsdata)
}