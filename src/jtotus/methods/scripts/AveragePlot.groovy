/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 *
 *
 * http://blogs.sun.com/geertjan/entry/netbeans_groovy_editor_in_a
 *
 */

package jtotus.rulebase

import jtotus.common.StockType;
import jtotus.methods.SimpleMovingAvg;
import jtotus.threads.PortfolioDecision;
import jtotus.common.Helper;
import jtotus.engine.Engine;
import jtotus.gui.graph.GraphPacket;
import jtotus.methods.PeriodClosingPrice;
import jtotus.methods.PotentialWithIn;

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
Engine engine = Engine.getInstance()
dispatcher = new PortfolioDecision()
//logger = new jtotus.graph.GraphSender(engine);
method = new jtotus.methods.SimpleMovingAvg(dispatcher)
method.run()

StockType stock = new StockType("Fortum Oyj")
PeriodClosingPrice period = new PeriodClosingPrice(stock);

Float max =  period.getMaxValue()
Float min = period.getMinValue();
println stock.getHexName() +  " Max:" + max + " Min:" + min + "\n";




//Thread potential = new Thread(new PotentialWithIn());
//potential.start();

println "DONE for AveragePlot"

