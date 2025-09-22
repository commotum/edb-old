/*
 * Decompiled with CFR 0.152.
 */
package datomic.valcache;

public interface IServer {
    public Object connection_count();

    public Object running_count();

    public Object pending_count();

    public Object handled_count();
}

