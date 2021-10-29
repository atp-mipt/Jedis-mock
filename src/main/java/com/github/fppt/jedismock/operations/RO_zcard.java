package com.github.fppt.jedismock.operations;

import com.github.fppt.jedismock.datastructures.RMMap;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.List;
import java.util.Map;

class RO_zcard extends AbstractRedisOperation {

    RO_zcard(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    @Override
    Slice response() {
        Slice key = params().get(0);
        final RMMap mapDBObj = getMapFromBase(key);
        final Map<Slice, Double> map = mapDBObj.getStoredData();
        if (map == null || map.isEmpty()) return Response.integer(0);
        return Response.integer(map.size());
    }
}
