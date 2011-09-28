rluc.preload <- function(luc.pathToLibs, luc.pathToClasses, workingDir) {
  
#luc.pathToLibs <- paste("/home/house/NetBeansProjects/JLucrum/dist/lib");
#luc.pathToClasses <- paste("/home/house/NetBeansProjects/JLucrum/build/classes");

Sys.setenv(TZ="GMT")
#Set working path
#setwd("~/Dropbox/jlucrum/rengine");
setwd(workingDir);

library(rJava);
library(tseries);
library(quantmod);
library(gtools)
require(ggplot2)
#library(fGarch)
source("~/Dropbox/jlucrum/rexperimental/external/itall.R")
source("~/Dropbox/jlucrum/rexperimental/functions/network.r")
.jinit(classpath=luc.pathToClasses, force.init=T)


command <- paste("ls ", luc.pathToLibs, sep="");
list.of.files <- system(command, intern=T);  

for (file in list.of.files) {
   print(paste(luc.pathToLibs,file, sep="/"))
  .jaddClassPath(paste(luc.pathToLibs,file, sep="/"))
}  

jluc.stockNames <<- c("Cargotec Oyj", "Elisa Oyj", 
"Fortum Oyj", "Kemira Oyj", "KONE Oyj", 
"Konecranes Oyj", "Metso Oyj", "Neste Oil", 
"Nokia Oyj", "Nokian Renkaat Oyj", "Nordea Bank AB", 
"Outokumpu Oyj", "Outotec Oyj", 
"Pohjola Bank A","Rautaruukki Oyj","Pohjola Bank A", 
"Sampo Oyj A", "Sanoma Oyj", "Stora Enso Oyj A", "TeliaSonera AB", 
"Tieto Oyj", "UPM-Kymmene Oyj", "Wärtsilä Corporation", "YIT Oyj");

DataFetcher <- J("org.jtotus.database.DataFetcher");
fetcher <<- new(DataFetcher);
}
