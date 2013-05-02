package org.jlucrum.datafetcher;

/**
 *
 * @author Evgeni Kappinen
 */

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class SourceCache { 

    private static final long serialVersionUID = 1L;
    private static SourceCache instance;
    private static HttpCache<String, HttpCache<String, Object>> cache;

    public class HttpCache<K, V> extends LinkedHashMap<K, V> {
        private int maxSize = 50;

        public HttpCache(int maxSize) {
            super(maxSize + 1, 1, true);
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Entry<K, V> entry) {
            return size() > this.maxSize;
        }
    }

    protected void initCache(int maxSize) {
        cache = new HttpCache<String,HttpCache<String,Object>>(maxSize);
    }

    public synchronized static SourceCache getInstance(int maxSize) {

        if (instance == null) {
            instance = new SourceCache();
            instance.initCache(maxSize);
        }
        return instance;
    }

    
    public synchronized Object getData(String stockName, String startDate, String endDate) {
        HttpCache<String, Object> records = cache.get(stockName);
        if (records == null) {
            return null;
        }
        return records.get(startDate + ":" + endDate);
    }
    
    
    public synchronized void putData(String stockName, String startDate, String endDate, Object record) {
        HttpCache<String, Object> records = cache.get(stockName);
        if (records == null) {
            records = new HttpCache<String, Object>(20);
            cache.put(stockName, records);
        }
        records.put(startDate + ":" + endDate, record);

        return;
    }
    
}