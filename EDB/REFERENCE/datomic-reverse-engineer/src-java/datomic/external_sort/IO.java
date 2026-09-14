/*
 * Decompiled with CFR 0.152.
 */
package datomic.external_sort;

public interface IO {
    public Object make_temp_file();

    public Object temp_file_size(Object var1);

    public Object delete_temp_file(Object var1);
}

