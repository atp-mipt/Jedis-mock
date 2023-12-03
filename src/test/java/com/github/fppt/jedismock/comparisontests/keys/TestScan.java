package com.github.fppt.jedismock.comparisontests.keys;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static redis.clients.jedis.params.ScanParams.SCAN_POINTER_START;

@ExtendWith(ComparisonBase.class)
public class TestScan {

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushDB();
    }

    @TestTemplate
    public void scanReturnsAllKey(Jedis jedis) {
        String key = "scankey:111";
        String key2 = "scankey:222";
        String value = "myvalue";
        jedis.set(key, value);
        jedis.set(key2, value);

        ScanResult<String> result = jedis.scan(SCAN_POINTER_START);

        assertEquals(SCAN_POINTER_START, result.getCursor());
        assertThat(result.getResult()).containsExactlyInAnyOrder(key, key2);
    }

    @TestTemplate
    public void scanReturnsMatchingKey(Jedis jedis) {
        String key = "scankeymatch:111";
        String key2 = "scankeymatch:222";
        String value = "myvalue";
        jedis.set(key, value);
        jedis.set(key2, value);

        ScanResult<String> result = jedis.scan(SCAN_POINTER_START,
                new ScanParams().match("scankeymatch:1*"));

        assertEquals(SCAN_POINTER_START, result.getCursor());
        assertThat(result.getResult()).containsExactly(key);
    }

    @TestTemplate
    public void scanIterates(Jedis jedis) {
        String value = "myvalue";
        for (int i = 0; i < 20; i++) {
            jedis.set("scankeyi:" + i, value);
        }

        ScanResult<String> result = jedis.scan(ScanParams.SCAN_POINTER_START,
                new ScanParams().match("scankeyi:1*").count(10));

        assertNotEquals(ScanParams.SCAN_POINTER_START, result.getCursor());
    }

    @TestTemplate
    public void scanDoesNotReturnExpiredKeys(Jedis jedis) throws InterruptedException {
        jedis.hset("test", "key", "value");
        jedis.expire("test", 1L);

        assertEquals(Collections.singletonList("test"), jedis.scan(ScanParams.SCAN_POINTER_START).getResult());
        Thread.sleep(2000);
        assertEquals(Collections.emptyList(), jedis.scan(ScanParams.SCAN_POINTER_START).getResult());
    }
}
