rluc.preload <- function(luc.pathToLibs, luc.pathToClasses, workingDir) {
  
#luc.pathToLibs <- paste("/home/house/NetBeansProjects/JLucrum/dist/lib");
#luc.pathToClasses <- paste("/home/house/NetBeansProjects/JLucrum/build/classes");

#Set working path
#setwd("~/Dropbox/jlucrum/rengine");
setwd(workingDir);

library(rJava);
library(tseries);
library(quantmod);

.jinit(classpath=luc.pathToClasses)


command <- paste("ls ", luc.pathToLibs, sep="");
list.of.files <- system(command, intern=T);  

for (file in list.of.files) {
   print(paste(luc.pathToLibs,file, sep="/"))
  .jaddClassPath(paste(luc.pathToLibs,file, sep="/"))
}  

DataFetcher <- J("org.jtotus.database.DataFetcher");
fetcher = new(DataFetcher);

}
