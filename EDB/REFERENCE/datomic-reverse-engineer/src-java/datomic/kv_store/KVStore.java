/*
 * Decompiled with CFR 0.152.
 */
package datomic.kv_store;

public interface KVStore {
    public Object put(Object var1);

    public Object get(Object var1, Object var2);

    public Object delete(Object var1, Object var2);

    public Object close();
}

