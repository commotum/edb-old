/*
 * Decompiled with CFR 0.152.
 */
package datomic.external_sort;

public interface ExternalSort {
    public Object consume_iter(Object var1);

    public Object consume_files_iter(Object var1, Object var2);

    public Object merge_step();
}

