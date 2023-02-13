package dev.schmarrn.lighty.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Used to securely share Data between computing and rendering thread.
 * Implemented using a "double buffer" approach. There exist two
 * HashMaps, only one gets written to at a time, the other only gets
 * read from.
 *
 * @param <K> The Key of the HashMap. Intended to be a BlockPos.
 * @param <V> Value of the HashMap. Here you can store all the needed Data for your LightyMode.
 */
public class ModeCache<K, V> {
    private final HashMap<K, V> bankOne = new HashMap<>();
    private final HashMap<K, V> bankTwo = new HashMap<>();
    private boolean firstActive = true; // if true: bankOne is the bank that can be modified

    private Map<K, V> getReadMap() {
        return firstActive ? bankTwo : bankOne;
    }

    private Map<K, V> getWriteMap() {
        return firstActive ? bankOne : bankTwo;
    }

    /**
     * Iterates over every entry in the cache. Changing any value has no effect, as it will
     * get overridden when the next swap occurs.
     *
     * @param action The action to be performed on every entry.
     */
    public void forEach(BiConsumer<K, V> action) {
        getReadMap().forEach(action);
    }

    /**
     * Puts a value into the cache at the key.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    public void put(K key, V value) {
        getWriteMap().put(key, value);
    }

    /**
     * Swaps the write-map with the read-map.
     * After swapping, an empty HashMap is ready to be filled again
     * and the previously written values are now readable.
     * If your goal is to just implement a LightyMode: Leave this method alone.
     * Swapping is handled by Lighty itself, DO NOT CALL THIS. Thanks.
     */
    public void swap() {
        firstActive = !firstActive;

        HashMap<K, V> oldRenderBank = firstActive ? bankOne : bankTwo;
        oldRenderBank.clear();
    }

    /**
     * Clears both write and read map.
     */
    public void clear() {
        bankOne.clear();
        bankTwo.clear();
    }
}
