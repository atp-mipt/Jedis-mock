package com.github.fppt.jedismock.operations.sortedsets;

import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.operations.RedisCommand;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.storage.OperationExecutorState;

import java.util.Arrays;
import java.util.List;

@RedisCommand("bzpopmin")
public class BZPopMin extends BZPop {

    BZPopMin(OperationExecutorState state, List<Slice> params) {
        super(state, params);
    }

    @Override
    protected Slice popper(List<Slice> params) {
        List<Slice> result = new ZPop(base(), params, false).pop();
        return Response.array(Arrays.asList(Response.bulkString(params.get(0)), result.get(0), result.get(1)));
    }
}
