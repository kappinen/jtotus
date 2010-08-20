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



// Draw the past 20 days of stock

fortum = new jtotus.common.StockType("Fortum Oyj")

sender = new jtotus.graph.GraphSender(engine);
packet = new jtotus.graph.GraphPacket();
packet.seriesTitle = "Fortum Oyj"


date = Calendar.getInstance();

Date time = date.getTime();

packet.day = time.day
packet.month = time.month
packet.year = time.year
for (int i=0; i<15;i++) {
    packet.day -= i
    packet.result = fortum.fetchPastDayClosingPrice(i);
    sender.sentPacket("Fortum Oyj", packet)
}


