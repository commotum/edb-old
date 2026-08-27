/*
 * Decompiled with CFR 0.152.
 */
package datomic.cluster;

public interface ClusteredStore {
    public Object create_val(Object var1, Object var2);

    public Object create_val(Object var1, Object var2, Object var3);

    public Object get_val(Object var1);

    public Object delete(Object var1);

    public Object delete_reference(Object var1);

    public Object get_ref(Object var1);

    public Object set_ref(Object var1, Object var2, Object var3);

    public Object get_pod(Object var1);

    public Object get_pod_meta(Object var1);

    public Object update_pod_STAR_(Object var1, Object var2, Object var3, Object var4, Object var5);
}

