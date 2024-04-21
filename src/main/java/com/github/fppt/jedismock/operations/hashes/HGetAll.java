package com.github.fppt.jedismock.operations.hashes;

import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.operations.AbstractRedisOperation;
import com.github.fppt.jedismock.operations.RedisCommand;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RedisCommand("hgetall")
public class HGetAll extends AbstractRedisOperation {
    public HGetAll(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    @Override
    protected Slice response() {
        Slice hash = params().get(0);

        Map<Slice, Slice> fieldAndValueMap = base().getFieldsAndValues(hash);

        if (fieldAndValueMap == null) {
            fieldAndValueMap = new HashMap<>();
        }

        List<Slice> output = new ArrayList<>();

        fieldAndValueMap.forEach((key, value) -> {
            output.add(0, Response.bulkString(value));
            output.add(0, Response.bulkString(key));
        });

        return Response.array(output);
    }
}
