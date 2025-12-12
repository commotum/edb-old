/*
 * Decompiled with CFR 0.152.
 */
package datomic.queue;

public interface BlockingConsumer {
    public Object take();

    public Object poll_b(Object var1, Object var2);
}

