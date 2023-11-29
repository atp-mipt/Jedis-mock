package com.github.fppt.jedismock.datastructures.streams;

public class StreamErrors {
    public static final String ID_OVERFLOW_ERROR = "ERR The stream has exhausted the last possible ID, unable to add more items";
    public static final String TOP_ERROR = "ERR The ID specified in XADD is equal or smaller than the target stream top item";
    public static final String INVALID_ID_ERROR = "ERR Invalid stream ID specified as stream command argument";
    public static final String ZERO_ERROR = "ERR The ID specified in XADD must be greater than 0-0";
    public static final String SYNTAX_ERROR = "ERR syntax error";
    public static final String NOT_AN_INTEGER_ERROR = "ERR value is not an integer or out of range";
    public static final String LIMIT_OPTION_ERROR = "ERR syntax error, LIMIT cannot be used without the special ~ option";
}
