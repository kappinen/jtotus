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

package org.jtotus.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 *
 * @author Evgeni Kappinen
 */
public class LocalJDBCFactory {
    private static LocalJDBCFactory localFactory = null;
    private static JdbcConnectionPool pool = null;

    protected LocalJDBCFactory() {
        System.out.printf("Test\n");
        try {
            Class.forName("org.h2.Driver").newInstance();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LocalJDBCFactory.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (InstantiationException ex) {
            Logger.getLogger(LocalJDBCFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(LocalJDBCFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (pool == null) {
            pool = JdbcConnectionPool.create("jdbc:h2:~/.jtotus/local_database", "sa", "sa");
            pool.setMaxConnections(100);
            System.out.printf("Test 2:%s\n",pool.toString());
        }
    }



    public synchronized static LocalJDBCFactory getInstance() {
        if(localFactory == null) {
            localFactory = new LocalJDBCFactory();
        }
        
        return localFactory;
    }

    public synchronized LocalJDBC jdbcFactory() {
        LocalJDBC localJDBC = new LocalJDBC();
        localJDBC.setPool(pool);
        System.out.printf("Active connections:%d max:%d\n", pool.getActiveConnections(), pool.getMaxConnections());

        return localJDBC;
    }

    

}
