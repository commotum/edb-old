/*
 * Decompiled with CFR 0.152.
 */
package datomic.connector;

public interface NotificationHandler {
    public Object notify_sync(Object var1);

    public Object notify_data(Object var1);

    public Object notify_error(Object var1, Object var2);

    public Object notify_db(Object var1);

    public Object notify_index();
}

