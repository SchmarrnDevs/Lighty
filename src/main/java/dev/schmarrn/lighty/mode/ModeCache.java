package dev.schmarrn.lighty.mode;

import java.util.HashMap;
import java.util.Map;

public class ModeCache<K, V> {
    private final HashMap<K, V> bankOne = new HashMap<>();
    private final HashMap<K, V> bankTwo = new HashMap<>();
    private boolean firstActive = true; // if true: bankOne is the bank that can be modified

    public Map<K, V> getRenderBank() {
        return firstActive ? bankTwo : bankOne;
    }

    public void put(K key, V value) {
        HashMap<K, V> working = firstActive ? bankOne : bankTwo;

        working.put(key, value);
    }

    public void swap() {
        firstActive = !firstActive;

        HashMap<K, V> oldRenderBank = firstActive ? bankOne : bankTwo;
        oldRenderBank.clear();
    }

    public void clear() {
        bankOne.clear();
        bankTwo.clear();
    }
}
