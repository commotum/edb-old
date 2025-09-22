/*
 * Decompiled with CFR 0.152.
 */
package datomic.log;

public interface Log {
    public Object claim(Object var1);

    public Object get_root_val();

    public Object get_root_id();

    public Object adopt_root(Object var1, Object var2, Object var3);

    public Object append(Object var1, Object var2);

    public Object val_keys();
}

