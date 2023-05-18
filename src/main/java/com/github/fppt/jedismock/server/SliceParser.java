package com.github.fppt.jedismock.server;

import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.exception.EOFException;
import com.github.fppt.jedismock.exception.ParseErrorException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SliceParser {

    public static byte consumeByte(InputStream messageInput) throws EOFException {
        int b;
        try {
            b = messageInput.read();
        } catch (IOException e) {
            throw new EOFException();
        }
        if (b == -1) {
            throw new EOFException();
        }
        return (byte) b;
    }

    public static void expectByte(InputStream messageInput, byte c) throws ParseErrorException, EOFException {
        if (consumeByte(messageInput) != c) {
            throw new ParseErrorException();
        }
    }

    public static long consumeLong(InputStream messageInput) throws ParseErrorException {
        byte c;
        long ret = 0;
        boolean hasLong = false;
        while (true) {
            c = consumeByte(messageInput);
            if (c == '\r') {
                break;
            }
            if (!isNumber(c)) {
                throw new ParseErrorException();
            }
            ret = ret * 10 + c - '0';
            hasLong = true;
        }
        if (!hasLong) {
            throw new ParseErrorException();
        }
        return ret;
    }

    public static Slice consumeSlice(InputStream messageInput, long len) throws ParseErrorException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        for (long i = 0; i < len; i++) {
            bo.write(consumeByte(messageInput));
        }
        return Slice.create(bo.toByteArray());
    }

    public static long consumeCount(InputStream messageInput) throws ParseErrorException {
        expectByte(messageInput, (byte) '*');
        long count = consumeLong(messageInput);
        expectByte(messageInput, (byte) '\n');
        return count;
    }

    public static long consumeCount(byte[] message) throws ParseErrorException {
        InputStream stream = new ByteArrayInputStream(message);
        return consumeCount(stream);
    }

    public static int consumeInteger(byte[] message) throws ParseErrorException {
        InputStream stream = new ByteArrayInputStream(message);
        return consumeInteger(stream);
    }

    public static int consumeInteger(InputStream messageInput) throws ParseErrorException {
        expectByte(messageInput, (byte) ':');
        long count = consumeLong(messageInput);
        return (int) count;
    }

    private static boolean isNumber(byte c) {
        return '0' <= c && c <= '9';
    }

    public static Slice consumeParameter(InputStream messageInput) throws ParseErrorException {
        expectByte(messageInput, (byte) '$');
        long len = consumeLong(messageInput);
        expectByte(messageInput, (byte) '\n');
        Slice para = consumeSlice(messageInput, len);
        expectByte(messageInput, (byte) '\r');
        expectByte(messageInput, (byte) '\n');
        return para;
    }

    public static Slice consumeParameter(byte[] message) throws ParseErrorException {
        InputStream stream = new ByteArrayInputStream(message);
        return consumeParameter(stream);
    }
}
