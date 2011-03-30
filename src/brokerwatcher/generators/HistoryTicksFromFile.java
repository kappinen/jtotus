/*
    This file is part of jTotus.

    jTotus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jTotus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jTotus.  If not, see <http://www.gnu.org/licenses/>.
*/

package brokerwatcher.generators;
import com.espertech.esper.client.EPRuntime;
import brokerwatcher.eventtypes.StockTick;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.jtotus.config.ConfTickGenerator;
import org.jtotus.config.ConfigLoader;
/**
 *
 * @author Evgeni Kappinen
 */
public class HistoryTicksFromFile implements EsperEventGenerator {
    private static final String pathToTicks = "ticks";
    private ArrayList<String> filesWithTicks = new ArrayList<String>();
    private boolean debug = false;
    private String delimiter=",";
    
    
    private int indxStockName = 0;
    private int indxGetLastestBuy = 1;
    private int indxGetLastestHighest = 2;
    private int indxGetLastestLowest = 3;
    private int indxGetLastestPrice = 4;
    private int indxGetLastestSell = 5;
    private int indxVolume = 6;
    private int indxTradesSum = 7;
    private int indxTime = 8;
    
    public HistoryTicksFromFile(EPRuntime cepRT) {
        esperRuntime = cepRT;
        readFileNames();
    }
    
    public HistoryTicksFromFile(EPRuntime cepRT, String fileToRead) {
        esperRuntime = cepRT;
        filesWithTicks.add(fileToRead);
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the delimiter
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * @param delimiter the delimiter to set
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    private static class NameComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            int retValue = 0;
            String name1 = o1.toString();
            String name2 = o2.toString();


            String []splitName1 = name1.split("_");
            String []splitName2 = name2.split("_");

            if (splitName1.length < 3 || splitName2.length < 3) {
                return Integer.MIN_VALUE;
            }

            retValue += Integer.parseInt(splitName1[2]) - Integer.parseInt(splitName2[2]);
            if (retValue != 0) {
                return retValue * 100;
            }
            
            retValue += Integer.parseInt(splitName1[1]) - Integer.parseInt(splitName2[1]);
            if (retValue != 0) {
                return retValue * 10;
            }

            retValue += Integer.parseInt(splitName1[0]) - Integer.parseInt(splitName2[0]);
            return retValue;
        }
    }

    private void readFileNames() {

        File dirFile = new File(pathToTicks);

        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return;
        }

        File []files = dirFile.listFiles();
        for(int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().endsWith(".txt")) {
                filesWithTicks.add(file.getName());
            }
        }
        Collections.sort(filesWithTicks, new NameComparator());
    }
    
    public ArrayList<String> getFilesWithTicks() {
        return filesWithTicks;
    }

    public void setFileWithTicks(String fileWithTicks) {
        filesWithTicks.add(fileWithTicks);
    }

    private EPRuntime esperRuntime = null;
    private ConfTickGenerator config = null;

    
    public String call() throws Exception {
        StockTick tick = null;
        String line = null;
        String fileWithTicks = "TicksToFile.txt";
        boolean readHeader = true;

        ConfigLoader<ConfTickGenerator> loader = new ConfigLoader<ConfTickGenerator>("ConfTickGenerator");
        config = loader.getConfig();
        if (config == null) {
            config = new ConfTickGenerator();
            loader.storeConfig(config);
        }

        if (filesWithTicks.isEmpty()) {
            filesWithTicks.add(fileWithTicks);
        }

        if (debug) {
            for (int fileIndx = 0; fileIndx < filesWithTicks.size(); fileIndx++) {
                System.out.printf("Order is %s = %d\n", filesWithTicks.get(fileIndx), fileIndx);
            }
        }

        for (int fileIndx = 0; fileIndx < filesWithTicks.size(); fileIndx++) {
            fileWithTicks = filesWithTicks.get(fileIndx);

            System.out.printf("Starting to read ticks from %s\n", fileWithTicks);
            BufferedReader in = new BufferedReader(new FileReader(pathToTicks + File.separator + fileWithTicks));

            while ((line = in.readLine()) != null) {
                tick = new StockTick();

                if (readHeader) {
                    //TODO: dynamic assigment of indx variables.
                    readHeader = false;
                    continue;
                }

                String[] tickAsString = line.split(",");
                if (tickAsString.length<8) {
                    System.out.printf("File : % is corrupted?\n", fileWithTicks);
                    return null;
                }

                tick.setLatestBuy(Double.parseDouble(tickAsString[indxGetLastestBuy]));
                tick.setLatestHighest(Double.parseDouble(tickAsString[indxGetLastestHighest]));
                tick.setLatestLowest(Double.parseDouble(tickAsString[indxGetLastestLowest]));
                tick.setLatestPrice(Double.parseDouble(tickAsString[indxGetLastestPrice]));
                tick.setLatestSell(Double.parseDouble(tickAsString[indxGetLastestSell]));
                tick.setVolume(Double.parseDouble(tickAsString[indxVolume]));
                tick.setTradesSum(Double.parseDouble(tickAsString[indxTradesSum]));
                tick.setStockName(tickAsString[indxStockName]);
                tick.setTime(tickAsString[indxTime]);
                

                

//                for (int i = 0; i < tickAsString.length; i++) {
//                    String[] varAndValue = tickAsString[i].trim().split("=");
//                    if (varAndValue.length != 2) {
//                        System.err.printf("%s : File corrupted !\n", fileWithTicks);
//                        continue;
//                    }
//                    //System.out.printf("Tick data: tick[%d] = %s line = %s\n",i, tickAsString[i], line);
//                    if (varAndValue[0].compareToIgnoreCase("StockName") == 0) {
//                        tick.setStockName(varAndValue[1]);
//                    } else if (varAndValue[0].compareToIgnoreCase("GetLastestBuy") == 0) {
//                        tick.setLatestBuy(Double.parseDouble(varAndValue[1]));
//                    } else if (varAndValue[0].compareToIgnoreCase("GetLastestHighest") == 0) {
//                        tick.setLatestHighest(Double.parseDouble(varAndValue[1]));
//                    } else if (varAndValue[0].compareToIgnoreCase("GetLastestLowest") == 0) {
//                        tick.setLatestLowest(Double.parseDouble(varAndValue[1]));
//                    } else if (varAndValue[0].compareToIgnoreCase("GetLastestPrice") == 0) {
//                        tick.setLatestPrice(Double.parseDouble(varAndValue[1]));
//                    } else if (varAndValue[0].compareToIgnoreCase("GetLastestSell") == 0) {
//                        tick.setLatestSell(Double.parseDouble(varAndValue[1]));
//                    } else if (varAndValue[0].compareToIgnoreCase("Volume") == 0) {
//                        tick.setVolume(Double.parseDouble(varAndValue[1]));
//                    } else if (varAndValue[0].compareToIgnoreCase("TradesSum") == 0) {
//                        tick.setTradesSum(Double.parseDouble(varAndValue[1]));
//                    } else if (varAndValue[0].compareToIgnoreCase("Time") == 0) {
//                        tick.setTime(varAndValue[1]);
//                    }
//                }

                if (tick.getVolume() == 0 && tick.getTradesSum() == 0) {
                        continue;
                    }
                
                esperRuntime.sendEvent(tick);
                //Thread.sleep(config.sleepBetweenTicks / 10);
                Thread.sleep(40);
            }
            System.out.printf("Done with %s\n", fileWithTicks);
            in.close();
        }

        return null;
    }



}
