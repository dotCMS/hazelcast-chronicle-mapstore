package com.dotcms.hazelcast.mapstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hazelcast.core.MapStore;

import net.openhft.chronicle.map.ChronicleMap;


/**
 * This class implements a CronicleMap based
 * mapsstore for hazelcast.  The pattern it uses is 1
 * map per region
 * @author will
 *
 */
public class CronicleMapStore implements MapStore<String, Object> {

    final ChronicleMap<String, Object> cache;
    final String region;
    final long entries;

    public CronicleMapStore(String region) {
        this(region,0);

    }

    public CronicleMapStore(String region, long entries) {
        this.region = region.toLowerCase();
        this.entries  = (entries==0) ?  HazelCroniclePropertyBundle.getLongProperty("croniclemap." + this.region + ".size",
                        HazelCroniclePropertyBundle.getLongProperty("croniclemap.default.size", 1000)) : entries;
        
        this.cache = initCache();


    }

    private ChronicleMap<String, Object> initCache() {
        return ChronicleMap.of(String.class, Object.class)
            .name(region)
            .entries(entries)
            .averageKey("this-is-a-normal-key-that-we.call-though-it-can-be-longer-than-it-should-be")
            .averageValueSize(10000)
            .create();
    }



    @Override
    public Object load(String key) {
        return cache.getOrDefault(key, null);
    }

    @Override
    public Map<String, Object> loadAll(Collection<String> keys) {
        Map<String, Object> map = new HashMap<>();
        for (String key : loadAllKeys()) {
            map.put(key, load(key));
        }
        return map;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        Iterable<String> iter = cache.keySet();
        List<String> keys = new ArrayList<>();
        iter.forEach(keys::add);
        return keys;

    }

    @Override
    public void store(String key, Object value) {
        cache.put(key, value);

    }

    @Override
    public void storeAll(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            store(entry.getKey(), entry.getValue());
        }

    }

    @Override
    public void delete(String key) {
        cache.remove(key);

    }

    @Override
    public void deleteAll(Collection<String> keys) {
        for (String key : keys) {
            delete(key);
        }

    }

}
