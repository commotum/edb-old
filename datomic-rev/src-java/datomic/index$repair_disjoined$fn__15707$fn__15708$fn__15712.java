/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713;
import datomic.index$repair_disjoined$fn__15707$fn__15708$fn__15712$fn__15721;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$repair_disjoined$fn__15707$fn__15708$fn__15712
extends AFunction {
    Object db;
    Object idxsort;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"disjoined-datoms");
    public static final Keyword const__2 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__3 = RT.keyword(null, (String)"history");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__9 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__10 = RT.keyword(null, (String)"event");
    public static final Keyword const__11 = RT.keyword((String)"index", (String)"repair-disjoined-datoms");
    public static final Keyword const__12 = RT.keyword(null, (String)"index");
    public static final Keyword const__13 = RT.keyword(null, (String)"mids");
    public static final Keyword const__14 = RT.keyword(null, (String)"hists");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"update-in");
    public static final Keyword const__16 = RT.keyword(null, (String)"indexing");
    public static final Keyword const__17 = RT.keyword(null, (String)"threw");

    public index$repair_disjoined$fn__15707$fn__15708$fn__15712(Object object, Object object2) {
        this.db = object;
        this.idxsort = object2;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object mids = ((IFn)const__1.getRawRoot()).invoke(this.db, (Object)const__2, this.idxsort);
            Object hists = ((IFn)const__1.getRawRoot()).invoke(this.db, (Object)const__3, this.idxsort);
            Object datoms2 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke(mids)), ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke(hists)));
            Object object2 = ((IFn)const__8.getRawRoot()).invoke(datoms2);
            if (object2 != null && object2 != Boolean.FALSE) {
                index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713 counts = new index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713();
                Logger logger = LoggerFactory.getLogger((String)"datomic.index");
                if (logger.isInfoEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    Object[] objectArray2 = new Object[8];
                    objectArray2[0] = const__10;
                    objectArray2[1] = const__11;
                    objectArray2[2] = const__12;
                    objectArray2[3] = this.idxsort;
                    objectArray2[4] = const__13;
                    Object object3 = mids;
                    mids = null;
                    objectArray2[5] = ((IFn)counts).invoke(object3);
                    objectArray2[6] = const__14;
                    index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713 index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713 = counts;
                    counts = null;
                    Object object4 = hists;
                    hists = null;
                    objectArray2[7] = ((IFn)index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713).invoke(object4);
                    logger2.info((String)((IFn)const__9.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray2)));
                }
                this.db = null;
                this.idxsort = null;
                Object object5 = datoms2;
                datoms2 = null;
                object = ((IFn)const__15.getRawRoot()).invoke(this.db, (Object)Tuple.create((Object)const__16, (Object)this.idxsort), (Object)new index$repair_disjoined$fn__15707$fn__15708$fn__15712$fn__15721(object5));
            } else {
                object = this.db;
                this.db = null;
            }
            objectArray[1] = object;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__17;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

