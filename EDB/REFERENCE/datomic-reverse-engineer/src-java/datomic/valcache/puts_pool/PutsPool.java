/*
 * Decompiled with CFR 0.152.
 */
package datomic.valcache.puts_pool;

public interface PutsPool {
    public Object submit(Object var1, Object var2, Object var3);

    public Object get_queued_put(Object var1);
}

