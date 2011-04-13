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

package org.jtotus.config;

import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author Evgeni Kappinen
 */
public class ConfPortfolio {
    private String portofoliName=null;
    public String[] inputListOfStocks;
    public double inputAssumedBudjet;
    public Calendar inputStartingDate = null;
    public Calendar inputEndingDate = null;
    public boolean useCurentDayAsEndingDate = true;
    public ArrayList<String> autoStartedMethods;

    public ConfPortfolio() {
        inputListOfStocks = fetchGUIStockNames();
        inputAssumedBudjet=6000;

        inputEndingDate = Calendar.getInstance();
        inputStartingDate = Calendar.getInstance();
        inputStartingDate.add(Calendar.DATE, -300);
        portofoliName = "OMXHelsinki";
        autoStartedMethods = new ArrayList<String>();
    }

    /**
     * @return the portofoliName
     */
    public String getPortofoliName() {
        return portofoliName;
    }

    /**
     * @param portofoliName the portofoliName to set
     */
    public void setPortofoliName(String portofoliName) {
        this.portofoliName = portofoliName;
    }

    public String[] fetchGUIStockNames() {
        GUIConfig config = null;
        ConfigLoader<GUIConfig> loader = new ConfigLoader<GUIConfig>("GUIConfig");

        //config = new GUIConfig();
        config = loader.getConfig();
        //if config does not exists create new one
        if (config == null) {
            config = new GUIConfig();
            loader.storeConfig(config);
        }

        return config.fetchStockNames();
        
    }

    public static synchronized ConfPortfolio getPortfolioConfig() {
        ConfPortfolio portfolioConfig;
        
        ConfigLoader<ConfPortfolio> configPortfolio =
                new ConfigLoader<ConfPortfolio>("OMXHelsinki");

        portfolioConfig = configPortfolio.getConfig();
        if (portfolioConfig == null) {
            //Load default values
            portfolioConfig = new ConfPortfolio();
            configPortfolio.storeConfig(portfolioConfig);
        }
        return portfolioConfig;
    }

    public boolean isAutoStared(String methodName) {
        return autoStartedMethods.contains(methodName);
    }

    public void setAutoStared(String methodName) {
        autoStartedMethods.add(methodName);
    }
}
