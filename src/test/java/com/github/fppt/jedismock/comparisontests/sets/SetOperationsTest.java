package com.github.fppt.jedismock.comparisontests.sets;

import com.github.fppt.jedismock.comparisontests.ComparisonBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonBase.class)
public class SetOperationsTest {

    @BeforeEach
    public void setUp(Jedis jedis) {
        jedis.flushAll();
    }

    @TestTemplate
    public void whenAddingToASet_EnsureTheSetIsUpdated(Jedis jedis) {
        String key = "my-set-key";
        Set<String> mySet = new HashSet<>(Arrays.asList("a", "b", "c", "d"));

        //Add everything from the set
        mySet.forEach(value -> jedis.sadd(key, value));

        //Get it all back
        assertEquals(mySet, jedis.smembers(key));
    }

    @TestTemplate
    public void whenDuplicateValuesAddedToSet_ReturnsAddedValuesCountOnly(Jedis jedis) {
        String key = "my-set-key-sadd";
        assertEquals(3, jedis.sadd(key, "A", "B", "C", "B"));
        assertEquals(1, jedis.sadd(key, "A", "C", "E", "B"));
    }

    @TestTemplate
    public void whenAddingToASet_ensureCountIsUpdated(Jedis jedis) {
        String key = "my-counted-set-key";
        Set<String> mySet = new HashSet<>(Arrays.asList("d", "e", "f"));

        //Add everything from the set
        mySet.forEach(value -> jedis.sadd(key, value));

        //Get it all back
        assertEquals(mySet.size(), jedis.scard(key));
    }

    @TestTemplate
    public void whenCalledForNonExistentSet_ensureScardReturnsZero(Jedis jedis) {
        String key = "non-existent";
        assertEquals(0, jedis.scard(key));
    }

    @TestTemplate
    public void whenRemovingFromASet_EnsureTheSetIsUpdated(Jedis jedis) {
        String key = "my-set-key";
        Set<String> mySet = new HashSet<>(Arrays.asList("a", "b", "c", "d"));

        //Add everything from the set
        mySet.forEach(value -> jedis.sadd(key, value));

        // Remove an element
        mySet.remove("c");
        mySet.remove("d");
        mySet.remove("f");
        long removed = jedis.srem(key, "c", "d", "f");

        //Get it all back
        assertEquals(mySet, jedis.smembers(key));
        assertEquals(2, removed);
    }

    @TestTemplate
    public void whenPoppingFromASet_EnsureTheSetIsUpdated(Jedis jedis) {
        String key = "my-set-key-spop";
        Set<String> mySet = new HashSet<>(Arrays.asList("a", "b", "c", "d"));

        //Add everything from the set
        mySet.forEach(value -> jedis.sadd(key, value));

        String poppedValue;
        do {
            poppedValue = jedis.spop(key);
            if (poppedValue != null) {
                assertTrue(mySet.contains(poppedValue), "Popped value not in set");
            }
        } while (poppedValue != null);
    }

    @TestTemplate
    public void poppingManyKeys(Jedis jedis) {
        String key = "my-set-key-spop";
        jedis.sadd(key, "a", "b", "c", "d");
        assertEquals(3,
                jedis.spop(key, 3).size());
        assertEquals(1, jedis.scard(key));
    }

    @TestTemplate
    public void ensureSismemberReturnsCorrectValues(Jedis jedis) {
        String key = "my-set-key-sismember";
        jedis.sadd(key, "A", "B");
        assertTrue(jedis.sismember(key, "A"));
        assertFalse(jedis.sismember(key, "C"));
        assertFalse(jedis.sismember(key + "-nonexistent", "A"));
    }

    @TestTemplate
    public void whenUsingHsinter_EnsureSetIntersectionIsReturned(Jedis jedis) {
        String key1 = "my-set-key-1";
        Set<String> mySet1 = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        String key2 = "my-set-key-2";
        Set<String> mySet2 = new HashSet<>(Arrays.asList("b", "d", "e", "f"));
        String key3 = "my-set-key-3";
        Set<String> mySet3 = new HashSet<>(Arrays.asList("b", "e", "f"));

        Set<String> expectedIntersection1 = new HashSet<>(Arrays.asList("b", "d"));
        Set<String> expectedIntersection2 = new HashSet<>(Collections.singletonList("b"));

        //Add everything from the sets
        mySet1.forEach(value -> jedis.sadd(key1, value));
        mySet2.forEach(value -> jedis.sadd(key2, value));
        mySet3.forEach(value -> jedis.sadd(key3, value));

        Set<String> intersection = jedis.sinter(key1, key2);
        assertEquals(expectedIntersection1, intersection);

        intersection = jedis.sinter(key1, key2, key3);
        assertEquals(expectedIntersection2, intersection);
    }

    @TestTemplate
    public void whenUsingSInterStore_testIntersectionIsStorred(Jedis jedis) {
        String key1 = "set1";
        String key2 = "set2";
        Set<String> mySet1 = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        Set<String> mySet2 = new HashSet<>(Arrays.asList("b", "d", "e", "f"));

        Set<String> expectedIntersection = new HashSet<>(Arrays.asList("b", "d"));

        //Add everything from the sets
        mySet1.forEach(value -> jedis.sadd(key1, value));
        mySet2.forEach(value -> jedis.sadd(key2, value));

        String destination = "set3";

        Long elementsInIntersection = jedis.sinterstore(destination, key1, key2);
        assertEquals(2, elementsInIntersection);

        assertEquals(expectedIntersection, jedis.smembers(destination));
    }

    @TestTemplate
    public void sUnionTest(Jedis jedis) {
        String key1 = "set1";
        String key2 = "set2";
        String key3 = "set3";
        Set<String> mySet1 = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        Set<String> mySet2 = new HashSet<>(Arrays.asList("c"));
        Set<String> mySet3 = new HashSet<>(Arrays.asList("a", "c", "e", "f"));

        Set<String> expectedUnion = new HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f"));

        //Add everything from the sets
        mySet1.forEach(value -> jedis.sadd(key1, value));
        mySet2.forEach(value -> jedis.sadd(key2, value));
        mySet3.forEach(value -> jedis.sadd(key3, value));


        Set<String> result = jedis.sunion(key1, key2, key3);
        assertEquals(6, result.size());
        assertEquals(expectedUnion, result);
    }

    @TestTemplate
    public void sUnionStoreTest(Jedis jedis) {
        String key1 = "set1";
        String key2 = "set2";
        String key3 = "set3";
        Set<String> mySet1 = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        Set<String> mySet2 = new HashSet<>(Arrays.asList("c"));
        Set<String> mySet3 = new HashSet<>(Arrays.asList("a", "c", "e", "f"));

        Set<String> expectedUnion = new HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f"));

        //Add everything from the sets
        mySet1.forEach(value -> jedis.sadd(key1, value));
        mySet2.forEach(value -> jedis.sadd(key2, value));
        mySet3.forEach(value -> jedis.sadd(key3, value));

        String destination = "set3";

        Long elementsInUnion = jedis.sunionstore(destination, key1, key2, key3);
        assertEquals(6, elementsInUnion);

        assertEquals(expectedUnion, jedis.smembers(destination));
    }

    @TestTemplate
    public void sDiffTwoSetsTest(Jedis jedis) {
        String key1 = "set1";
        String key2 = "set2";
        Set<String> mySet1 = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        Set<String> mySet2 = new HashSet<>(Arrays.asList("c", "d", "e"));

        Set<String> expectedDifference = new HashSet<>(Arrays.asList("a", "b"));

        //Add everything from the sets
        mySet1.forEach(value -> jedis.sadd(key1, value));
        mySet2.forEach(value -> jedis.sadd(key2, value));


        Set<String> result = jedis.sdiff(key1, key2);
        assertEquals(2, result.size());
        assertEquals(expectedDifference, result);
    }

    @TestTemplate
    public void sDiffThreeSetsTest(Jedis jedis) {
        String key1 = "set1";
        String key2 = "set2";
        String key3 = "set3";
        Set<String> mySet1 = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        Set<String> mySet2 = new HashSet<>(Arrays.asList("c"));
        Set<String> mySet3 = new HashSet<>(Arrays.asList("a", "c", "e"));

        Set<String> expectedDifference = new HashSet<>(Arrays.asList("b", "d"));

        //Add everything from the sets
        mySet1.forEach(value -> jedis.sadd(key1, value));
        mySet2.forEach(value -> jedis.sadd(key2, value));
        mySet3.forEach(value -> jedis.sadd(key3, value));


        Set<String> result = jedis.sdiff(key1, key2, key3);
        assertEquals(2, result.size());
        assertEquals(expectedDifference, result);
    }

    @TestTemplate
    public void sDiffStoreTest(Jedis jedis) {
        String key1 = "set1";
        String key2 = "set2";
        Set<String> mySet1 = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        Set<String> mySet2 = new HashSet<>(Arrays.asList("c", "d", "e"));

        Set<String> expectedDifference = new HashSet<>(Arrays.asList("a", "b"));

        //Add everything from the sets
        mySet1.forEach(value -> jedis.sadd(key1, value));
        mySet2.forEach(value -> jedis.sadd(key2, value));

        String destination = "set3";

        Long elementsInDifference = jedis.sdiffstore(destination, key1, key2);
        assertEquals(2, elementsInDifference);

        assertEquals(expectedDifference, jedis.smembers(destination));
    }
    

    @TestTemplate
    public void testFailingGetOperation(Jedis jedis) {
        jedis.sadd("my-set-key", "a", "b", "c", "d");
        assertTrue(
                assertThrows(JedisDataException.class, () -> jedis.get("my-set-key"))
                        .getMessage().startsWith("WRONGTYPE"));
    }

    @TestTemplate
    public void testSaddNonUTF8binary(Jedis jedis) {
        byte[] msg = new byte[]{(byte) 0xbe};
        jedis.sadd("foo".getBytes(), msg);
        byte[] newMsg = jedis.spop("foo".getBytes());
        assertArrayEquals(msg, newMsg);
    }

    @TestTemplate
    public void testSMoveExistingElement(Jedis jedis) {
        jedis.sadd("myset", "one", "two");
        jedis.sadd("myotherset", "three");
        assertEquals(1, jedis.smove("myset", "myotherset", "two"));
        assertEquals(Collections.singleton("one"), jedis.smembers("myset"));
        assertEquals(new HashSet<>(Arrays.asList("two", "three")),
                jedis.smembers("myotherset"));
    }

    @TestTemplate
    public void testSMoveNonExistingElement(Jedis jedis) {
        jedis.sadd("myset", "one", "two");
        jedis.sadd("myotherset", "three");
        assertEquals(0, jedis.smove("myset", "myotherset", "four"));
        assertEquals(new HashSet<>(Arrays.asList("one", "two")),
                jedis.smembers("myset"));
        assertEquals(Collections.singleton("three"),
                jedis.smembers("myotherset"));
    }
}
