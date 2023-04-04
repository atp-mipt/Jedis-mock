package com.github.fppt.jedismock.operations.scripting;

import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.operations.AbstractRedisOperation;
import com.github.fppt.jedismock.operations.RedisCommand;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.storage.OperationExecutorState;
import com.github.fppt.jedismock.storage.RedisBase;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RedisCommand("eval")
public class Eval extends AbstractRedisOperation {
    private static final String SCRIPT_PARAM_ERROR = "Wrong number of arguments for EVAL";
    private static final String SCRIPT_COMPILE_ERROR = "Error compiling script (new function returned nil)";
    private static final String SCRIPT_RUNTIME_ERROR = "Error running script (call to function returned nil)";
    private final Globals globals = JsePlatform.standardGlobals();

    private final OperationExecutorState state;

    public Eval(final RedisBase base, final List<Slice> params, final OperationExecutorState state) {
        super(base, params);
        this.state = state;
    }

    @Override
    public Slice response() {
        if (params().size() < 2) {
            return Response.error(SCRIPT_PARAM_ERROR);
        }
        final String script = params().get(0).toString()
                .replace("redis.", "redis:");
        int numKeys = Integer.parseInt(params().get(1).toString());
        final List<LuaValue> args = params()
                .subList(2, params().size()).stream()
                .map(Slice::toString)
                .map(LuaValue::valueOf)
                .collect(Collectors.toList());
        globals.set("KEYS", embedLuaListToValue(args.subList(0, numKeys)));
        globals.set("ARGV", embedLuaListToValue(args.subList(numKeys, args.size())));
        globals.set("redis", CoerceJavaToLua.coerce(new RedisCallback(state)));
        try {
            final LuaValue luaScript = globals.load(script);
            final LuaValue result = luaScript.call();
            if (result.isnil()) {
                return Response.error(SCRIPT_RUNTIME_ERROR);
            }
            return resolveResult(result);
        } catch (LuaError e) {
            return Response.error(SCRIPT_COMPILE_ERROR);
        }
    }

    private static LuaTable embedLuaListToValue(final List<LuaValue> luaValues) {
        return LuaValue.listOf(luaValues.toArray(new LuaValue[0]));
    }

    private Slice resolveResult(LuaValue result) {
        switch (result.typename()) {
            case "string":
                return Response.bulkString(Slice.create(result.tojstring()));
            case "number":
                return Response.integer(result.tolong());
            case "table":
                final ArrayList<Slice> list = new ArrayList<>();
                for (int i = 0; i < result.length(); i++) {
                    list.add(resolveResult(result.get(i+1)));
                }
                return Response.array(list);
        }
        return Response.error(SCRIPT_RUNTIME_ERROR);
    }
}
