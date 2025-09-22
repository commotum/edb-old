/*
 * Decompiled with CFR 0.152.
 */
package datomic.cache;

public interface ICachedLookup {
    public Object valAtUncached(Object var1, Object var2);

    public Object getFromCache(Object var1, Object var2);
}

