/*
 * Decompiled with CFR 0.152.
 */
package datomic.connector;

public interface TransactorConnector {
    public Object endpoint();

    public Object admin_request_STAR_(Object var1, Object var2, Object var3);

    public Object create_notifier(Object var1, Object var2);

    public Object start_updater(Object var1, Object var2, Object var3);
}

