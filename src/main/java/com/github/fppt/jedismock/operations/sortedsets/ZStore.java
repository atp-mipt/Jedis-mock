package com.github.fppt.jedismock.operations.sortedsets;

import com.github.fppt.jedismock.datastructures.RMZSet;
import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.datastructures.ZSetEntry;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class ZStore extends AbstractByScoreOperation {

    protected static final String IS_WEIGHTS = "WEIGHTS";
    protected static final String IS_AGGREGATE = "AGGREGATE";
    protected static final String IS_WITHSCORES = "WITHSCORES";

    protected int startKeysIndex = 0;
    protected ArrayList<Double> weights;

    protected BiFunction<Double, Double, Double> aggregate = Double::sum;

    protected boolean withScores = false;

    ZStore(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    abstract protected RMZSet getResult(RMZSet zset1, RMZSet zset2, double weight);

    protected RMZSet getFinishedZSet() {
        int numKeys = Integer.parseInt(params().get(startKeysIndex).toString());
        parseParams(numKeys);
        RMZSet mapDBObj = new RMZSet();
        RMZSet temp = getZSet(params().get(startKeysIndex + 1));
        for (ZSetEntry entry :
                temp.entries(false)) {
            mapDBObj.put(entry.getValue(), entry.getScore() * weights.get(0));
        }
        for (int i = 1; i < numKeys; i++) {
           mapDBObj = getResult(mapDBObj, getZSet(params().get(startKeysIndex + i + 1)), weights.get(i));
        }

        return mapDBObj;
    }

    private RMZSet getZSet(Slice setName) {
        if (base().exists(setName)) {
            String typeName = base().getValue(setName).getTypeName();
            if ("zset".equalsIgnoreCase(typeName)) {
                return base().getZSet(setName);
            }
            if ("set".equalsIgnoreCase(typeName)) {
                RMZSet result = new RMZSet();
                for (Slice value : base().getSet(setName).getStoredData()) {
                    result.put(value, 1);
                }
                return result;
            }
        }
        return new RMZSet();
    }

   private void parseParams(int numKeys) {
       int curIndex = startKeysIndex + numKeys + 1;
       weights = new ArrayList<>(numKeys);
       for (int i = 0; i < numKeys; i++) {
           weights.add(1.0);
       }
       if ((params().size() > curIndex) && (IS_WEIGHTS.equalsIgnoreCase(params().get(curIndex).toString()))) {
           for (int i = 0; i < numKeys; i++) {
               weights.set(i, toDouble(params().get(curIndex + i + 1).toString()));
           }
           curIndex += numKeys + 1;
       }
       if ((params().size() > curIndex) && (IS_AGGREGATE.equalsIgnoreCase(params().get(curIndex).toString()))) {
           String aggParam = params().get(curIndex + 1).toString();
           if ("MIN".equalsIgnoreCase(aggParam)) {
               aggregate = Double::min;
           }
           if ("MAX".equalsIgnoreCase(aggParam)) {
               aggregate = Double::max;
           }
           if ("SUM".equalsIgnoreCase(aggParam)) {
               aggregate = Double::sum;
           }
           curIndex += 2;
       }
       if ((params().size() > curIndex) && (IS_WITHSCORES.equalsIgnoreCase(params().get(curIndex).toString()))) {
           withScores = true;
       }
   }

    protected long getResultSize() {
        Slice keyDest = params().get(0);
        startKeysIndex = 1;
        RMZSet mapDBObj = getFinishedZSet();
        if (!mapDBObj.isEmpty()) {
            base().putValue(keyDest, mapDBObj);
        }
        return mapDBObj.size();
    }

    protected List<Slice> getResultArray() {
        startKeysIndex = 0;
        return getFinishedZSet().entries(false).stream()
                .flatMap(e -> withScores
                        ? Stream.of(e.getValue(),
                        Slice.create(String.valueOf(Math.round(e.getScore()))))
                        : Stream.of(e.getValue()))
                .map(Response::bulkString)
                .collect(Collectors.toList());
    }
}
