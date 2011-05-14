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

/**
 *
 * @author Evgeni Kappinen
 */
public class MainMethodConfig {
   public String inputPortfolio=null;
   public boolean inputPrintResults = false;
   public boolean inputPerfomDecision = true;
   private boolean autoStart = false;
   private boolean isForTraining = false;
   public String inputNormilizerType=null;

    //Methods sets, if available.
    //Should be represented in procents.
    public Double outputSuccessRate=null;

   public MainMethodConfig() {

        inputPortfolio = "OMXHelsinki";

   }

    /**
     * @return the autoStart
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * @param autoStart the autoStart to set
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
}
