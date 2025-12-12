/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 */
package datomic.db;

import clojure.lang.IFn;
import datomic.impl.db.IDatum;
import datomic.iter.Iter;

public interface IDb {
    public Object getNextT();

    public Object keywordOf(Object var1);

    public Object idOf(Object var1);

    public Object getAsOfT();

    public Object getSinceT();

    public Object getRaw();

    public Object getFilter();

    public Iter seekEAVT(IDatum var1);

    public Iter seekAVET(IDatum var1);

    public Iter seekAEVT(IDatum var1);

    public Iter seekRAET(IDatum var1);

    public IFn getFn(Object var1);
}

