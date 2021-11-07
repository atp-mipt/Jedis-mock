package com.github.fppt.jedismock.operations;

import com.github.fppt.jedismock.datastructures.RMDataStructure;
import com.github.fppt.jedismock.datastructures.RMList;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static com.github.fppt.jedismock.Utils.convertToInteger;
import static com.github.fppt.jedismock.Utils.deserializeObject;
import static com.github.fppt.jedismock.Utils.serializeObject;

@RedisCommand("lrem")
class RO_lrem extends AbstractRedisOperation {
    private final int directedNumRemove;
    private final Slice target;

    private boolean isDeletingElement(Slice element, int numRemoved) {
        return element.equals(target) && (directedNumRemove == 0 || numRemoved < Math.abs(directedNumRemove));
    }

    RO_lrem(RedisBase base, List<Slice> params) {
        super(base, params);
        directedNumRemove = convertToInteger(new String(params().get(1).data()));
        target = params().get(2);
    }

    Slice response(){
        Slice key = params().get(0);
        Slice target = params().get(2);
        RMList listObj = base().getList(key);
        if(listObj == null){
            return Response.integer(0);
        }

        List<Slice> list = listObj.getStoredData();

        //Determine the directionality of the deletions
        int numRemoved = 0;
        if(directedNumRemove < 0){
            ListIterator<Slice> iterator = list.listIterator(list.size());
            while (iterator.hasPrevious()) {
                Slice element = iterator.previous();
                if(isDeletingElement(element, numRemoved)) {
                    iterator.remove();
                    numRemoved++;
                }
            }
        } else {
            Iterator<Slice> iterator = list.listIterator();
            while (iterator.hasNext()) {
                Slice element = iterator.next();
                if(isDeletingElement(element, numRemoved)) {
                    iterator.remove();
                    numRemoved++;
                }
            }
        }

        return Response.integer(numRemoved);
    }
}
