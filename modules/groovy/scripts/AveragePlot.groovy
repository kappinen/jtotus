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
 *
 *
 *
 * http://blogs.sun.com/geertjan/entry/netbeans_groovy_editor_in_a
 *
 */

package jtotus.rulebase

import org.jtotus.common.StockType;
import org.jtotus.methods.SimpleMovingAvg;
import org.jtotus.threads.PortfolioDecision;
import org.jtotus.common.Helper;
import org.jtotus.engine.Engine;
import org.jtotus.gui.graph.GraphPacket;
import org.jtotus.methods.PeriodClosingPrice;
import org.jtotus.methods.PotentialWithIn;

//Example 1
class Callee {
  void hello() {
     println "hello, world"
  }
}

c = new Callee()
c.hello()


//Example 2
def name='Example 2'
println "Hello $name!"


//Example 3
//Class.forName("jtotus.common.StockName").newInstance()
//Class.forName("jtotus.threads.Dispatcher").newInstance()
//Class.forName("jtotus.engine.SimpleMovingAvg").newInstance()
//Class.forName("jtotus.engine.SimpleMovingAvg").newInstance()
//Class.forName("jtotus.database.DataFetcher").newInstance()

//Example 4

Helper help = Helper.getInstance()
System.out.println(help)









//Example 5

method = new org.jtotus.methods.SimpleMovingAvg()
method.run()

StockType stock = new StockType("Fortum Oyj")
PeriodClosingPrice period = new PeriodClosingPrice(stock);

Float max =  period.getMaxValue()
Float min = period.getMinValue();
println stock.getHexName() +  " Max:" + max + " Min:" + min + "\n";




//Thread potential = new Thread(new PotentialWithIn());
//potential.start();

println "DONE for AveragePlot"

