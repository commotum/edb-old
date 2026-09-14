/*
 * Decompiled with CFR 0.152.
 */
package datomic.backup;

public interface Storage {
    public Object store(Object var1, Object var2);

    public Object list_keys(Object var1);

    public Object exists_QMARK_(Object var1);

    public Object retrieve(Object var1);
}

