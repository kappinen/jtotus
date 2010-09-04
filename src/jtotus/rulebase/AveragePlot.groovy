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

import jtotus.common.StockType
import jtotus.engine.SimpleMovingAvg
import jtotus.threads.Dispatcher
import jtotus.common.Helper;
import jtotus.engine.Engine;
import jtotus.graph.GraphPacket;


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
dispatcher = new Dispatcher()
//logger = new jtotus.graph.GraphSender(engine);
method = new jtotus.engine.SimpleMovingAvg(dispatcher)
method.run()



println "DONE for AveragePlot"

