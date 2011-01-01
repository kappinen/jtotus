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

package brokerwatcher;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jtotus.common.StockTick;
import org.jtotus.threads.MethodFuture;

/**
 *
 * @author Evgeni Kappinen
 */
public class BrokerWatcher implements Callable{
    private ExecutorService threadExecutor = null;
    private MethodFuture<String> futureTask = null;
    private HashMap<String, EPStatement> statements = null;
    private EPServiceProvider cep = null;

    public BrokerWatcher() {
        statements = new HashMap<String, EPStatement>();
        threadExecutor = Executors.newCachedThreadPool();

        Configuration cepConfig = new Configuration();
        cepConfig.addEventType("StockTick", StockTick.class.getName());

        cep = EPServiceProviderManager.getProvider("BrokerWatcher", cepConfig);

    }

    public void addStatement(String statement, UpdateListener listener) {

        EPStatement eps = statements.get(statement);
        if (eps == null) {
            EPAdministrator cepAdm = cep.getEPAdministrator();
            eps = cepAdm.createEPL(statement);
        }
        
        eps.addListener(listener);
    }


    public Object call() {
        

        TickGenerator tickGenerator = new TickGenerator(cep.getEPRuntime());
        futureTask = new MethodFuture<String>(tickGenerator);
        threadExecutor.execute(futureTask);

        return null;
    }

}
