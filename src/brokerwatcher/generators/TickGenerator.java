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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jtotus.config.ConfTickGenerator;
import org.jtotus.config.ConfigLoader;
import org.jtotus.config.GUIConfig;
import org.jtotus.network.NetworkTickConnector;
import org.jtotus.network.NordnetConnect;

/**
 *
 * @author Evgeni Kappinen
 */
public class TickGenerator implements EsperEventGenerator {

    private EPRuntime esperEngine = null;
    private NetworkTickConnector networkTicks = null;
    private ConfTickGenerator config = null;
    private String[] stockList = null;

    public TickGenerator(EPRuntime cepRT) {
        esperEngine = cepRT;

        ConfigLoader<ConfTickGenerator> loader
                = new ConfigLoader<ConfTickGenerator>("ConfTickGenerator");
        
        config = loader.getConfig();
        if (config == null) {
            config = new ConfTickGenerator();
            loader.storeConfig(config);
        }
    }

    boolean initialize() {

        if (networkTicks != null) {
            return true;
        }
        
        ConfigLoader<GUIConfig> stockLoader = new ConfigLoader<GUIConfig>("GUIConfig");
        stockList = stockLoader.getConfig().fetchStockNames();

        networkTicks = new NordnetConnect();

        return networkTicks.connect();
    }

    public String call() throws Exception {
        StockTick tick = null;

        if (!this.initialize()) {
            System.err.printf("Failed to initialize..\n");
            return null;
        }

        System.out.printf("Stargin TickGenerator..\n");
        while (true) {

            if (timeout()) {
                continue;
            }

            for (String stockName : stockList) {
                tick = networkTicks.getTick(stockName);
                if (tick != null) {
                    esperEngine.sendEvent(tick);
                }
            }

            System.out.printf("Sleeping (%d) ...\n", config.sleepBetweenTicks);
            Thread.sleep(config.sleepBetweenTicks);
        }
    }

    private boolean timeout() {
        DateTimeZone timeZone = null;
        DateTime time = null;
        long startTickerTime = 0;
        long endTickerTime = 0;
        boolean toContinue = false;

        timeZone = DateTimeZone.forID(config.timeZone);
        
        startTickerTime = config.start_hour * 60 + config.start_minute;
        endTickerTime = config.end_hour * 60 + config.end_minute;
        try {
            time = new DateTime(timeZone);
            if (time.getMinuteOfDay() < startTickerTime) {

                long minutesToSleep = Math.abs(time.getMinuteOfDay() - startTickerTime);
                System.out.printf("Sleeping (%d) minutes ... Starting 1 at:%d:%d\n", minutesToSleep, config.start_hour, config.start_minute);
                Thread.sleep(minutesToSleep * 60 * 1000);
                toContinue = true;

            } else if (time.getMinuteOfDay() > endTickerTime) {
                long minutesToSleep = 24 * 60 - time.getMinuteOfDay();
                minutesToSleep += startTickerTime;
                System.out.printf("Sleeping (%d) minutes ... Starting 2 at:%d:%d\n", minutesToSleep, config.start_hour, config.start_minute);
                Thread.sleep(minutesToSleep * 60 * 1000);
                toContinue = true;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(TickGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return toContinue;
    }

}
