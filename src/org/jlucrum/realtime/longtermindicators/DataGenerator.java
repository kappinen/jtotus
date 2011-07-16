package org.jlucrum.realtime.longtermindicators;

import org.jtotus.config.ConfPortfolio;
import org.jtotus.config.ConfigLoader;

import java.util.HashMap;

/**
 * This file is part of JTotus.
 * <p/>
 * jTotus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * jTotus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with jTotus.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
* Created by IntelliJ IDEA.
* Author: Evgeni Kappinen
* Date: 4/28/11
* Time: 7:45 PM
*/
public class DataGenerator {
    ConfPortfolio portfolioConfig = null;
    HashMap<String, double[]> stockValues;


    void invoke() {
        ConfigLoader<ConfPortfolio> configPortfolio = new ConfigLoader<ConfPortfolio>("OMXHelsinki");

        portfolioConfig = configPortfolio.getConfig();
        if (portfolioConfig == null) {
            //Load default values
            portfolioConfig = new ConfPortfolio();
            configPortfolio.storeConfig(portfolioConfig);
        }
    }

    void sendEvent() {
        stockValues = new HashMap<String, double[]>(portfolioConfig.inputListOfStocks.length);


    }


}
