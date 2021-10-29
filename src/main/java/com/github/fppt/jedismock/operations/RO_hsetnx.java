package com.github.fppt.jedismock.operations;

import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.List;

class RO_hsetnx extends RO_hset {
    RO_hsetnx(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    Slice hsetValue(Slice key1, Slice key2, Slice value){
        Slice foundValue = base().getSlice(key1, key2);
        if(foundValue == null){
            base().putSlice(key1, key2, value, -1L);
        }
        return foundValue;
    }
}
