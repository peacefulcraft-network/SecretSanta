package net.peacefulcraft.secretsanta.io;

public interface GenericConfig {
    boolean getBoolean(String paramString);

    boolean getBoolean(String paramString, boolean paramBoolean);

    String getString(String paramString);

    String getString(String paramString1, String paramString2);

    int getInteger(String paramString);

    int getInteger(String paramString, int paramInt);
}
