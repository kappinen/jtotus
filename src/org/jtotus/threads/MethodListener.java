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
package org.jtotus.threads;

import org.jtotus.common.MethodResults;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.config.ConfigLoader;
import org.jtotus.config.ResultsOfLongTermMethods;
import org.jtotus.engine.Engine;
import org.jtotus.gui.MethodResultsPrinter;

/**
 *
 * @author Evgeni Kappinen
 */
public class MethodListener implements InterfaceMethodListner {
     
    public void putResults(MethodResults results) {

        ConfigLoader<ResultsOfLongTermMethods> loader =
                new ConfigLoader<ResultsOfLongTermMethods>(ConfPortfolio.getPathToResults());

        ResultsOfLongTermMethods resultsFile = loader.getConfig();
        if (resultsFile == null) {
            resultsFile = new ResultsOfLongTermMethods();
        }
        resultsFile.add(results.getMethodName(), results);
        loader.storeConfig(resultsFile);
        
        Engine engine = Engine.getInstance();
        results.printToConsole();
        MethodResultsPrinter printer = engine.getResultsPrinter();
        printer.drawResults(results);

    }
}
