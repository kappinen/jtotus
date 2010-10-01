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

package jtotus.methods;

import java.util.concurrent.Callable;
import jtotus.common.MethodResults;
import com.tictactec.ta.lib.Core;
import java.util.Date;
import java.util.List;
import jtotus.common.StockType;


/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibRSI  implements MethodEntry, Callable<MethodResults>{
    
    private final Core core = new Core();
    private final StockType stockType = null;
    /*Stock list */
    private List<String> stockNames = null;
    private List<Date>resutlsForDates = null;
    


    public String getMethName() {
        String tmp = this.getClass().getName();
        return tmp.substring(tmp.lastIndexOf(".")+1,tmp.length());
    }

    public boolean isCallable() {
        return true;
    }

    public void run() {
        

        

    }

    public MethodResults call() throws Exception {
        MethodResults res = null;


        return res;
    }


}
