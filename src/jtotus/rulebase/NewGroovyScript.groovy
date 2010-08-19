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
stockName = new jtotus.common.StockType("Fortum Oyj")
println stockName.getHexName();





//Example 5
dispatcher = new Dispatcher()
//logger = new jtotus.graph.GraphSender(engine);
method = new jtotus.engine.SimpleMovingAvg(dispatcher)
method.run()
