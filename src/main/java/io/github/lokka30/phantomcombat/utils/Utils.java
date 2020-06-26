package io.github.lokka30.phantomcombat.utils;

import java.util.Arrays;
import java.util.List;

public class Utils {

    public Utils() {
    }

    public List<String> getSupportedServerVersions() {
        return Arrays.asList("1.15", "1.16");
    }

    public int getLatestSettingsVersion() {
        return 10;
    }

    public int getLatestMessagesVersion() {
        return 4;
    }

    public int getLatestDataVersion() {
        return 1;
    }
}
