package com.linuxgods.kreiger.util;

public interface Configuration {

    String getString(Key<String> name);
    void setString(Key<String> name, String value);

    void save();

    interface Key<T> {
        String getName();
        T getDefault();

        static <T> Key<T> of(String name, T defaultValue) {
            return new Key<T>() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public T getDefault() {
                    return defaultValue;
                }
            };
        }
    }
}
