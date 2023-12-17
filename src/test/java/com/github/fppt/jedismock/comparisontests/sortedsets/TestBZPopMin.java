package com.github.fppt.jedismock.comparisontests.sortedsets;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.util.KeyValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(ComparisonBase.class)
public class TestBZPopMin {

    private static final String ZSET_KEY_1 = "myzset";
    private static final String ZSET_KEY_2 = "ztmp";

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushDB();

    }

    @TestTemplate
    public void testBZPopMinFromSingleExistingSortedSet(Jedis jedis) {
        jedis.zadd(ZSET_KEY_1, 1, "b");
        jedis.zadd(ZSET_KEY_1, 0, "a");
        jedis.zadd(ZSET_KEY_1, 2, "c");
        KeyValue<String, Tuple> result = jedis.bzpopmin(0, ZSET_KEY_2, ZSET_KEY_1, "aaa");
        KeyValue<String, Tuple> expected = KeyValue.of(ZSET_KEY_1, new Tuple("a", 0.0));

        assertThat(result).isEqualTo(expected);
    }

    @TestTemplate
    public void testBZPopMinFromMultiplyExistingSortedSet(Jedis jedis) {
        jedis.zadd(ZSET_KEY_1, 0, "a");
        jedis.zadd(ZSET_KEY_1, 1, "b");
        jedis.zadd(ZSET_KEY_1, 2, "c");

        jedis.zadd(ZSET_KEY_2, 3, "d");
        jedis.zadd(ZSET_KEY_2, 4, "e");
        jedis.zadd(ZSET_KEY_2, 5, "f");
        KeyValue<String, Tuple>  result = jedis.bzpopmin(0, ZSET_KEY_2, ZSET_KEY_1, "aaa");
        KeyValue<String, Tuple>  expected = KeyValue.of(ZSET_KEY_2, new Tuple("d", 3.0));

        assertThat(result).isEqualTo(expected);
    }

    @TestTemplate
    public void testBZPopMinFromEmptySortedSetAndTimeOut(Jedis jedis) {
        long timeout = 1;
        long startTime = System.currentTimeMillis();
        assertThatThrownBy(() ->
                jedis.bzpopmin(timeout, ZSET_KEY_2, ZSET_KEY_1, "aaa"))
                .isInstanceOf(NullPointerException.class);
        long finishTime = System.currentTimeMillis();
        assertThat(finishTime - startTime).isGreaterThanOrEqualTo(timeout * 1000);
    }
}
