/*
 * Decompiled with CFR 0.152.
 */
package datomic;

public interface Datom {
    public Object e();

    public Object a();

    public Object v();

    public Object tx();

    public boolean added();

    public Object get(int var1);
}

