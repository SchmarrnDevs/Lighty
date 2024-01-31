package dev.schmarrn.lighty.config;

public abstract class ConfigSerDe {
    abstract String serialize();
    abstract void deserialize(String value);
}
