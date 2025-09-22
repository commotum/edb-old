/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ddb_values$get_deitem
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"tableName");
    public static final Keyword const__1 = RT.keyword(null, (String)"key");
    public static final Var const__2 = RT.var((String)"datomic.ddb", (String)"create-key");
    public static final Var const__4 = RT.var((String)"datomic.ddb", (String)"get-item");
    public static final Var const__5 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__6 = RT.keyword((String)"ddb-values", (String)"get-deitem-consistent-retry");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__8 = RT.keyword(null, (String)"consistentRead");
    public static final Var const__9 = RT.var((String)"datomic.ddb", (String)"deitem");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object ddb_client, Object table, Object id) {
        Object object;
        Object temp__5457__auto__20418;
        Object object2;
        Object or__5238__auto__20417;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__0;
        Object object3 = table;
        table = null;
        objectArray[1] = object3;
        objectArray[2] = const__1;
        objectArray[3] = ((IFn)const__2.getRawRoot()).invoke(id);
        IPersistentMap lmap = RT.mapUniqueKeys((Object[])objectArray);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = ((IFn)const__4.getRawRoot()).invoke(ddb_client, (Object)lmap);
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        Object object6 = or__5238__auto__20417 = object5;
        if (object6 != null && object6 != Boolean.FALSE) {
            object2 = or__5238__auto__20417;
            or__5238__auto__20417 = null;
        } else {
            Logger logger = LoggerFactory.getLogger((String)"datomic.ddb-values");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = const__6;
                Object object7 = id;
                id = null;
                objectArray2[1] = object7;
                logger2.info((String)((IFn)const__5.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray2)));
            }
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object8 = ddb_client;
            ddb_client = null;
            IPersistentMap iPersistentMap = lmap;
            lmap = null;
            Object object9 = ((IFn)const__4.getRawRoot()).invoke(object8, ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap, (Object)const__8, (Object)Boolean.TRUE));
            object2 = iLookupThunk2.get(object9);
            if (iLookupThunk2 == object2) {
                __thunk__1__ = __site__1__.fault(object9);
                object2 = __thunk__1__.get(object9);
            }
        }
        Object object10 = temp__5457__auto__20418 = object2;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object result2;
            Object object11 = temp__5457__auto__20418;
            temp__5457__auto__20418 = null;
            Object object12 = result2 = object11;
            result2 = null;
            object = ((IFn)const__9.getRawRoot()).invoke(object12);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return ddb_values$get_deitem.invokeStatic(object4, object5, object6);
    }
}

