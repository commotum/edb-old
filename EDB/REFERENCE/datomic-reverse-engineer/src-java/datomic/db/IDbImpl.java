/*
 * Decompiled with CFR 0.152.
 */
package datomic.db;

import datomic.db.IElementImpl;
import java.util.ArrayList;

public interface IDbImpl {
    public Object eq(Object var1);

    public Object elementAt(Object var1);

    public Object addKeyword(Object var1, Object var2);

    public Object addElement(IElementImpl var1);

    public Object growElements(Object var1);

    public Object addData(Object var1, ArrayList var2, Object var3);

    public Object acceptData(Object var1);

    public Object acceptDataCheck(Object var1, Object var2);

    public Object getRawId();
}

