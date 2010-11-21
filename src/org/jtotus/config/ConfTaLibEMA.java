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
public class ConfTaLibEMA extends MainMethodConfig{

    //Basic configuration
    public int inputEMAPeriod = 10;

    //Strategy Decision
    public String inputEMADecisionPeriod;

    public ConfTaLibEMA() {
        super();
        //Decision
        inputEMADecisionPeriod = "[3-30]{1}";
        outputSuccessRate = new Double(0.0f);
    }

}
