package com.github.fppt.jedismock.operations.sortedsets;

import com.github.fppt.jedismock.datastructures.RMZSet;
import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.datastructures.ZSetEntry;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.List;

abstract class AbstractZInter extends ZStore {

    AbstractZInter(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    protected RMZSet getResult(RMZSet zset1, RMZSet zset2, double weight) {
        RMZSet result = new RMZSet();
        for (ZSetEntry entry :
                zset1.entries(false)) {
            if (zset2.hasMember(entry.getValue())) {
                result.put(entry.getValue(), aggregate.apply(entry.getScore(), zset2.getScore(entry.getValue()) * weight));
            }
        }
        return result;
    }
}
