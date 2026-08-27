/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$accept_index$fn__13644$adopt__13645;
import datomic.db$accept_index$fn__13644$fn__13648;
import datomic.db$accept_index$fn__13644$fn__13650;
import datomic.db$accept_index$fn__13644$fn__13652;
import datomic.db$accept_index$fn__13644$fn__13654;
import datomic.db$accept_index$fn__13644$fn__13656;
import datomic.db.Db;
import datomic.db.IndexSet;

public final class db$accept_index$fn__13644
extends AFunction {
    Object db;
    Object rev;
    Object nextT;
    Object basisT;
    Object mid_index;
    Object history;
    Object root_id;
    Object index;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"mem-index-set");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__5 = RT.keyword(null, (String)"memidx");
    public static final Keyword const__6 = RT.keyword(null, (String)"memlog");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"trim-log");
    public static final Keyword const__8 = RT.keyword(null, (String)"index");
    public static final Keyword const__9 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__10 = RT.keyword(null, (String)"indexBasisT");
    public static final Keyword const__11 = RT.keyword(null, (String)"history");
    public static final Keyword const__12 = RT.keyword(null, (String)"index-root-id");
    public static final Keyword const__13 = RT.keyword(null, (String)"index-rev");
    public static final Var const__14 = RT.var((String)"datomic.fulltext-index", (String)"update-fulltext");
    public static final Var const__15 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__16 = RT.var((String)"datomic.iter", (String)"filter");
    public static final Var const__17 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__18 = RT.var((String)"datomic.db", (String)"recalc-elements");
    public static final Keyword const__19 = RT.keyword(null, (String)"threw");

    public db$accept_index$fn__13644(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.db = object;
        this.rev = object2;
        this.nextT = object3;
        this.basisT = object4;
        this.mid_index = object5;
        this.history = object6;
        this.root_id = object7;
        this.index = object8;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object new_memidx = const__1.getRawRoot();
            db$accept_index$fn__13644$adopt__13645 adopt = new db$accept_index$fn__13644$adopt__13645(this.nextT);
            Object iset = ((Db)this.db).memidx;
            Object eavtr = ((IFn)const__2.getRawRoot()).invoke((Object)new db$accept_index$fn__13644$fn__13648(iset, (Object)adopt, new_memidx));
            Object avetr = ((IFn)const__2.getRawRoot()).invoke((Object)new db$accept_index$fn__13644$fn__13650(iset, (Object)adopt, new_memidx));
            Object aevtr = ((IFn)const__2.getRawRoot()).invoke((Object)new db$accept_index$fn__13644$fn__13652(iset, (Object)adopt, new_memidx));
            Object object = iset;
            iset = null;
            db$accept_index$fn__13644$adopt__13645 db$accept_index$fn__13644$adopt__13645 = adopt;
            adopt = null;
            Object object2 = new_memidx;
            new_memidx = null;
            Object raetr = ((IFn)const__2.getRawRoot()).invoke((Object)new db$accept_index$fn__13644$fn__13654(object, (Object)db$accept_index$fn__13644$adopt__13645, object2));
            Object object3 = eavtr;
            eavtr = null;
            Object eavt2 = ((IFn)const__3.getRawRoot()).invoke(object3);
            Object object4 = avetr;
            avetr = null;
            Object avet2 = ((IFn)const__3.getRawRoot()).invoke(object4);
            Object object5 = aevtr;
            aevtr = null;
            Object aevt2 = ((IFn)const__3.getRawRoot()).invoke(object5);
            Object object6 = raetr;
            raetr = null;
            Object raet2 = ((IFn)const__3.getRawRoot()).invoke(object6);
            Object ft_basis = ((IFn)const__4.getRawRoot()).invoke(this.db, (Object)const__5, (Object)new IndexSet(eavt2, avet2, aevt2, raet2, null), (Object)const__6, ((IFn)const__7.getRawRoot()).invoke(((Db)this.db).memlog, this.basisT), (Object)const__8, this.index, (Object)const__9, this.mid_index, (Object)const__10, this.basisT, (Object)const__11, this.history, (Object)const__12, this.root_id, (Object)const__13, this.rev);
            Object ft = ((IFn)const__14.getRawRoot()).invoke(null, ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke((Object)new db$accept_index$fn__13644$fn__13656(ft_basis), ((IFn)const__17.getRawRoot()).invoke(aevt2))));
            Object object7 = ft_basis;
            ft_basis = null;
            Object object8 = eavt2;
            eavt2 = null;
            Object object9 = avet2;
            avet2 = null;
            Object object10 = aevt2;
            aevt2 = null;
            Object object11 = raet2;
            raet2 = null;
            Object object12 = ft;
            ft = null;
            objectArray[1] = ((IFn)const__18.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object7, (Object)const__5, (Object)new IndexSet(object8, object9, object10, object11, object12)), this.basisT);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__19;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

