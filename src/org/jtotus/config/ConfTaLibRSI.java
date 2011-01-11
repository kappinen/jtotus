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
public class ConfTaLibRSI extends MainMethodConfig {

    public int inputRSIPeriod = 14;
    public String inputRSIDecisionPeriod = null;
    public String inputRSILowestThreshold = null;
    public String inputRSIHigestThreshold = null;
    public int outputRSIHigestThreshold = 0;
    public int outputRSILowestThreshold = 0;

    public ConfTaLibRSI() {
        inputNormilizerType = "SimpleMinWins";
        //Decision
        inputRSIDecisionPeriod = "int[3-20]{4}";
        inputRSILowestThreshold = "int[1-50]{5}";
        inputRSIHigestThreshold = "int[50-99]{5}";
    }
}
