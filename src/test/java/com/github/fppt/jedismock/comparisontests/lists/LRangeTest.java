package com.github.fppt.jedismock.comparisontests.lists;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;

import java.util.Collections;

@ExtendWith(ComparisonBase.class)
public class LRangeTest {
    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
    }

    @TestTemplate
    public void whenUsingLRange_checkOutOfRangeNegativeEndIndex(Jedis jedis) {
        String key = "lrange_key";
        jedis.lpush(key, "1", "2", "3");

        Assertions.assertEquals(Collections.emptyList(), jedis.lrange(key, 0, -5));
    }
}
