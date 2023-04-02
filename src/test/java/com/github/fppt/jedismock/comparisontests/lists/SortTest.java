package com.github.fppt.jedismock.comparisontests.lists;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.shaded.com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.args.SortingOrder;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.SortingParams;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(ComparisonBase.class)
public class SortTest {

    private static final String key = "sort_key";
    private static final String numerical_sort_key = "numerical_sort_key";
    private static final String store_sort_key = "store_sort_key";

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();

        jedis.rpush(key, "a", "b", "c", "1", "2", "3", "c", "c");
        jedis.rpush(numerical_sort_key, "5", "4", "3", "2", "1");
    }

    @TestTemplate
    public void whenUsingSort_EnsureSortsNumerical(Jedis jedis) {
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), jedis.sort(numerical_sort_key));
        assertEquals(Arrays.asList("5", "4", "3", "2", "1"), jedis.sort(numerical_sort_key, new SortingParams().sortingOrder(SortingOrder.DESC)));
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), jedis.sort(numerical_sort_key, new SortingParams().sortingOrder(SortingOrder.ASC)));
    }

    @TestTemplate
    public void whenUsingSort_EnsureSortsAlphabetically(Jedis jedis) {
        List<String> result = Arrays.asList("1", "2", "3", "a", "b", "c", "c", "c");
        assertEquals(result, jedis.sort(key, new SortingParams().sortingOrder(SortingOrder.ASC).alpha()));
        assertEquals(Lists.reverse(result), jedis.sort(key, new SortingParams().sortingOrder(SortingOrder.DESC).alpha()));
    }

    @TestTemplate
    public void whenUsingSort_EnsureStores(Jedis jedis) {
        assertEquals(5, jedis.sort(numerical_sort_key, new SortingParams().sortingOrder(SortingOrder.DESC), store_sort_key));
        assertEquals(Arrays.asList("5", "4", "3", "2", "1"), jedis.lrange(numerical_sort_key, 0, 5));
    }

    @TestTemplate
    public void whenUsingSort_EnsureThrowsOnInvalidType(Jedis jedis) {
        JedisDataException e = Assertions.assertThrows(JedisDataException.class, () -> jedis.sort(key));

        assertEquals(e.getMessage(), "ERR One or more scores can't be converted into double");
    }
}
