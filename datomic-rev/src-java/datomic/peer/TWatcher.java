/*
 * Decompiled with CFR 0.152.
 */
package datomic.peer;

public interface TWatcher {
    public Object sync_t(Object var1);

    public Object sync_background_t(Object var1, Object var2);

    public Object wait_for_future_t(Object var1);

    public Object release_pending_syncs(Object var1);
}

