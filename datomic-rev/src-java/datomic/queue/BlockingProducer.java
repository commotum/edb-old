/*
 * Decompiled with CFR 0.152.
 */
package datomic.queue;

public interface BlockingProducer {
    public Object put(Object var1);

    public Object offer_b(Object var1, Object var2);
}

