package com.github.fppt.jedismock.operations;

import com.github.fppt.jedismock.datastructures.RMList;
import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.storage.OperationExecutorState;

import java.util.List;

@RedisCommand("brpop")
class RO_brpop extends RO_bpop {

    RO_brpop(OperationExecutorState state, List<Slice> params) {
        super(state, params);
    }

    @Override
    AbstractRedisOperation popper(List<Slice> params) {
        return new RO_rpop(base(), params);
    }
}
