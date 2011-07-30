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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.joda.time.DateTime;

/**
 *
 * @author Evgeni Kappinen
 * 
 * Reference: http://stackoverflow.com/questions/221525/how-would-you-implement-an-lru-cache-in-java-6
 */
public class LRUCache implements Cache {
    private LruCache<String, BigDecimal> cache= new LruCache<String, BigDecimal>(3000);
    
    @Override
    public void putValue(String stockName, DateTime date, BigDecimal value) {
        cache.put(stockName + date.toString(), value);
    }

    @Override
    public BigDecimal getValue(String stockName, DateTime date) {
        return cache.get(stockName + date.toString());
    }

    private class LruCache<A, B> extends LinkedHashMap<A, B> {
        private final int maxEntries;

        public LruCache(final int maxEntries) {
            super(maxEntries + 1, 1.0f, true);
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
            return super.size() > maxEntries;
        }
    }
}
