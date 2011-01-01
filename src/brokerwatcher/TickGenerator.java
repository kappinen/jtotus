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

package brokerwatcher;

import com.espertech.esper.client.EPRuntime;
import java.util.concurrent.Callable;
import org.jtotus.common.StockTick;
import org.jtotus.config.ConfTickGenerator;
import org.jtotus.config.ConfigLoader;
import org.jtotus.config.GUIConfig;
import org.jtotus.network.NetworkTickConnector;
import org.jtotus.network.NordnetConnect;

/**
 *
 * @author Evgeni Kappinen
 */
public class TickGenerator implements Callable <String>{
    private EPRuntime esperEngine = null;
    private NetworkTickConnector networkTicks = null;
    private ConfTickGenerator config = null;
    private String []stockList = null;
    
    public TickGenerator(EPRuntime cepRT) {
        esperEngine = cepRT;
    }


    boolean initialize() {

        ConfigLoader<ConfTickGenerator> loader = new ConfigLoader<ConfTickGenerator>("ConfTickGenerator");
        config = loader.getConfig();
        if (config == null) {
            config = new ConfTickGenerator();
            loader.storeConfig(config);
        }

        ConfigLoader<GUIConfig> stockLoader = new ConfigLoader<GUIConfig>("GUIConfig");
        stockList = stockLoader.getConfig().fetchStockNames();
        
        networkTicks = new NordnetConnect();

        return networkTicks.connect();
    }

    
    public String call() throws Exception {
        StockTick tick = null;

        if(!this.initialize()) {
            return null;
        }


            for (String stockName: stockList) {
                tick = networkTicks.getTick(stockName);
                if (tick != null) {
                    esperEngine.sendEvent(tick);
                }
            }
            
            Thread.sleep(config.sleepBetweenTicks);
            System.out.printf("Sleeping..\n");
        

        return null;
    }


}
