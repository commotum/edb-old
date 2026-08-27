/*
 * Decompiled with CFR 0.152.
 */
package datomic.memcached;

public interface RecoveringClientImpl {
    public Object rc_shutdown();

    public Object rc_get(Object var1);

    public Object rc_set(Object var1, Object var2, Object var3);

    public Object rc_reset_if_crashed();
}

