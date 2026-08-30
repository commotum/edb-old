/*
 * Decompiled with CFR 0.152.
 */
package datomic;

import datomic.Database;
import java.util.Set;

public interface Entity {
    public Object get(Object var1);

    public Entity touch();

    public Set<String> keySet();

    public Database db();
}

