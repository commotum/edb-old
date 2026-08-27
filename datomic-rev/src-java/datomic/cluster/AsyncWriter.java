/*
 * Decompiled with CFR 0.152.
 */
package datomic.cluster;

public interface AsyncWriter {
    public Object finish_writer();

    public Object sync_writes();
}

