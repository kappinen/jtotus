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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;


/**
 *
 * @author Evgeni Kappinen
 */
public class LocalJDBCFactory {
    private static final LocalJDBCFactory localFactory = new LocalJDBCFactory();
    private static JdbcConnectionPool pool;

    private LocalJDBCFactory() {
//        try {
//            Class.forName("org.h2.Driver").newInstance();
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(LocalJDBCFactory.class.getName()).log(Level.SEVERE, null, ex);
//            return;
//        } catch (InstantiationException ex) {
//            Logger.getLogger(LocalJDBCFactory.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(LocalJDBCFactory.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        if (pool == null) {
//                JdbcDataSource ds = new JdbcDataSource();
//                ds.setURL("jdbc:h2:~/.jtotus/local_database");
//                ds.setUser("sa");
//                ds.setPassword("sa");
//
////                Properties env = new Properties();
////                env.put("java.naming.factory.initial", "com.sun.jndi.cosnaming.CNCtxFactory");
////                Context ctx = new InitialContext(env);
////                ctx.bind("org.h2.jdbcx.JdbcDataSource", ds);
//                pool = JdbcConnectionPool.create(ds);
//                pool.setMaxConnections(200);
//
//        }
    }

    public synchronized static LocalJDBCFactory getInstance() {
        return localFactory;
    }

    public synchronized LocalJDBC jdbcFactory() {
        LocalJDBC localJDBC = new LocalJDBC();

        return localJDBC;
    }

}
