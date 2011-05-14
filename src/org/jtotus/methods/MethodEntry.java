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


package org.jtotus.methods;

import brokerwatcher.eventtypes.MarketData;
import java.util.concurrent.Callable;

import com.espertech.esper.client.UpdateListener;
import org.jtotus.common.MethodResults;


/**
 *
 * @author Evgeni Kappinen
 */
public interface MethodEntry extends Runnable, Callable<MethodResults>, UpdateListener {
    
    public String getMethName();

    //If Method supports return value this
    // method will return true
    public boolean isCallable();
    public MethodResults runCalculation();
    public MethodResults runCalculation(MarketData data);
    public void setMarketData(MarketData data);
}
