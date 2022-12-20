package com.github.fppt.jedismock;

import com.github.fppt.jedismock.datastructures.RMDataStructure;
import com.github.fppt.jedismock.datastructures.RMHash;
import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.operations.server.MockExecutor;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.server.ServiceOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class InterceptorMockServer {

    public static final int PORT = 39807;
    public static final Logger LOGGER = LoggerFactory.getLogger(InterceptorMockServer.class);

    private InterceptorMockServer() {

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LOGGER.info("Service starting...");
        RedisServer
                .newRedisServer(PORT)
                .setOptions(ServiceOptions.withInterceptor((state, roName, params) -> {
                    if ("config".equalsIgnoreCase(roName)) {
                        //Just a junk response instead of an error
                        return Response.bulkString(Slice.create("1"));
                    } else if ("debug".equalsIgnoreCase(roName)
                            && "object".equalsIgnoreCase(params.get(0).toString())
                    ) {
                        // Handling unsopported DEBUG OBJECT command
                        RMDataStructure cls = state.base().getHash(params.get(1));

                        // Currently it is supported only for RMHash.
                        // If you will add support for other data structures, insert their handling here
                        if (cls instanceof RMHash) {
                            RMHash hash = (RMHash) cls;
                            return Response.bulkString(Slice.create(hash.getMeta()));
                        }
                        return Response.bulkString(
                            Slice.create("DEBUG OBJECT command for this data structure is not yet supported")
                        );

                    } else if ("object".equalsIgnoreCase(roName)
                            && "encoding".equalsIgnoreCase(params.get(0).toString())
                    ) {
                        // Handling unsopported OBJECT ENCODING command
                        RMDataStructure cls = state.base().getHash(params.get(1));

                        // Currently it is supported only for RMHash.
                        // If you will add support for other data structures, insert their handling here
                        if (cls instanceof RMHash) {
                            RMHash hash = (RMHash) cls;
                            return Response.bulkString(Slice.create(hash.getEncoding()));
                        }
                        return Response.bulkString(
                            Slice.create("OBJECT ENCODING command for this data structure is not yet supported")
                        );

                    } else {
                        //Delegate execution to JedisMock which will mock the real Redis behaviour (when it can)
                        return MockExecutor.proceed(state, roName, params);
                    }
                }))
                .start();
        LOGGER.info("Service started at port {}.", PORT);
    }
}
