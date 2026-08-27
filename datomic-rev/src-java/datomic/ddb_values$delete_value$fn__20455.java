/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.ddb_values$delete_value$fn__20455$fn__20456;

public final class ddb_values$delete_value$fn__20455
extends AFunction {
    Object ddb_client;
    Object id;
    Object table;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__4 = RT.var((String)"datomic.ddb", (String)"get-item");
    public static final Keyword const__5 = RT.keyword(null, (String)"tableName");
    public static final Keyword const__6 = RT.keyword(null, (String)"key");
    public static final Var const__7 = RT.var((String)"datomic.ddb", (String)"create-key");
    public static final Keyword const__8 = RT.keyword(null, (String)"attributesToGet");
    public static final AFn const__9 = (AFn)Tuple.create((Object)"__n");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"dorun");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"range");
    public static final Object const__13 = 1L;
    public static final Var const__14 = RT.var((String)"datomic.ddb", (String)"delete-item");
    public static final Keyword const__15 = RT.keyword(null, (String)"returnValues");
    public static final Keyword const__16 = RT.keyword(null, (String)"threw");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"n"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public ddb_values$delete_value$fn__20455(Object object, Object object2, Object object3) {
        this.ddb_client = object;
        this.id = object2;
        this.table = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object temp__5457__auto__20468;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            ILookupThunk iLookupThunk = __thunk__1__;
            ILookupThunk iLookupThunk2 = __thunk__0__;
            Object object2 = ((IFn)const__4.getRawRoot()).invoke(this.ddb_client, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__5, this.table, const__6, ((IFn)const__7.getRawRoot()).invoke(this.id), const__8, const__9}));
            Object object3 = iLookupThunk2.get(object2);
            if (iLookupThunk2 == object3) {
                __thunk__0__ = __site__0__.fault(object2);
                object3 = __thunk__0__.get(object2);
            }
            Object object4 = RT.get((Object)object3, (Object)"__n");
            Object object5 = iLookupThunk.get(object4);
            if (iLookupThunk == object5) {
                __thunk__1__ = __site__1__.fault(object4);
                object5 = __thunk__1__.get(object4);
            }
            Object object6 = temp__5457__auto__20468 = object5;
            if (object6 != null && object6 != Boolean.FALSE) {
                Object n;
                Object object7 = temp__5457__auto__20468;
                temp__5457__auto__20468 = null;
                Object object8 = n = object7;
                n = null;
                ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke((Object)new ddb_values$delete_value$fn__20455$fn__20456(this.ddb_client, this.id, this.table), ((IFn)const__12.getRawRoot()).invoke(const__13, (Object)Numbers.num((long)Long.parseLong((String)object8)))));
                this.ddb_client = null;
                Object[] objectArray2 = new Object[6];
                objectArray2[0] = const__5;
                objectArray2[1] = this.table = null;
                objectArray2[2] = const__6;
                this.id = null;
                objectArray2[3] = ((IFn)const__7.getRawRoot()).invoke(this.id);
                objectArray2[4] = const__15;
                objectArray2[5] = "NONE";
                object = ((IFn)const__14.getRawRoot()).invoke(this.ddb_client, (Object)RT.mapUniqueKeys((Object[])objectArray2));
            } else {
                object = null;
            }
            objectArray[1] = object;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__16;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

