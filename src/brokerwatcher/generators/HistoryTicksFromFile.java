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
import java.io.FileReader;
import org.jtotus.config.ConfTickGenerator;
import org.jtotus.config.ConfigLoader;
/**
 *
 * @author Evgeni Kappinen
 */
public class HistoryTicksFromFile implements EsperEventGenerator {
    private String fileWithTicks = "TicksToFile.txt";

    public String getFileWithTicks() {
        return fileWithTicks;
    }

    public void setFileWithTicks(String fileWithTicks) {
        this.fileWithTicks = fileWithTicks;
    }

    private EPRuntime esperRuntime = null;
    private ConfTickGenerator config = null;

    public HistoryTicksFromFile(EPRuntime cepRT) {
        esperRuntime = cepRT;
    }
    public HistoryTicksFromFile(EPRuntime cepRT, String fileToRead) {
        esperRuntime = cepRT;
        this.fileWithTicks =  fileToRead;
    }


    public String call() throws Exception {
        StockTick tick = null;
        String line = null;

        ConfigLoader<ConfTickGenerator> loader = new ConfigLoader<ConfTickGenerator>("ConfTickGenerator");
        config = loader.getConfig();
        if (config == null) {
            config = new ConfTickGenerator();
            loader.storeConfig(config);
        }


        BufferedReader in = new BufferedReader(new FileReader(fileWithTicks));

        while((line = in.readLine()) != null){
            tick = new StockTick();
            
            String []tickAsString = line.split(",");
            for(int i=0;i<tickAsString.length;i++) {
                String []varAndValue = tickAsString[i].trim().split("=");
                if (varAndValue.length != 2) {
                    System.err.printf("%s : File corrupted !\n", fileWithTicks);
                    continue;
                }
                //System.out.printf("Tick data: tick[%d] = %s line = %s\n",i, tickAsString[i], line);
                if (varAndValue[0].compareToIgnoreCase("StockName") == 0) {
                    tick.setStockName(varAndValue[1]);
                }else if (varAndValue[0].compareToIgnoreCase("GetLastestBuy") == 0) {
                    tick.setLatestBuy( Double.parseDouble(varAndValue[1]));
                }else if (varAndValue[0].compareToIgnoreCase("GetLastestHighest") == 0) {
                    tick.setLatestHighest( Double.parseDouble(varAndValue[1]));
                }else if (varAndValue[0].compareToIgnoreCase("GetLastestLowest") == 0) {
                    tick.setLatestLowest( Double.parseDouble(varAndValue[1]));
                }else if (varAndValue[0].compareToIgnoreCase("GetLastestPrice") == 0) {
                    tick.setLatestPrice( Double.parseDouble(varAndValue[1]));
                }else if (varAndValue[0].compareToIgnoreCase("GetLastestSell") == 0) {
                    tick.setLatestSell( Double.parseDouble(varAndValue[1]));
                }else if (varAndValue[0].compareToIgnoreCase("Volume") == 0) {
                    tick.setVolume( Double.parseDouble(varAndValue[1]));
                }else if (varAndValue[0].compareToIgnoreCase("TradesSum") == 0) {
                    tick.setTradesSum( Double.parseDouble(varAndValue[1]));
                }else if (varAndValue[0].compareToIgnoreCase("Time") == 0) {
                    tick.setTime(varAndValue[1]);
                }
            }
            
            esperRuntime.sendEvent(tick);
            //Thread.sleep(config.sleepBetweenTicks / 10);
            Thread.sleep(200);
        }

        return null;
    }



}
