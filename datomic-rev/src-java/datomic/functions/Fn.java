/*
 * Decompiled with CFR 0.152.
 */
package datomic.functions;

import java.util.List;

public interface Fn {
    public String lang();

    public List<String> params();

    public String code();
}

