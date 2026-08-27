/*
 * Decompiled with CFR 0.152.
 */
package datomic.peer;

public interface RemoteConnection {
    public Object get_cluster();

    public Object get_olookup();

    public Object create_connection_state(Object var1, Object var2, Object var3);
}

