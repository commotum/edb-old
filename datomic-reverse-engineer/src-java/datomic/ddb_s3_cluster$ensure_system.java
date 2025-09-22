/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;

public final class ddb_s3_cluster$ensure_system
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"table-name");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"every?");
    public static final AFn const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"bucket"), (Object)RT.keyword(null, (String)"bucket-prefix"), (Object)RT.keyword(null, (String)"efs-path"), (Object)RT.keyword(null, (String)"region"), (Object)RT.keyword(null, (String)"table-name"));
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__12 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"every?"), Symbol.intern(null, (String)"storage-config"), Tuple.create((Object)RT.keyword(null, (String)"bucket"), (Object)RT.keyword(null, (String)"bucket-prefix"), (Object)RT.keyword(null, (String)"efs-path"), (Object)RT.keyword(null, (String)"region"), (Object)RT.keyword(null, (String)"table-name"))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 13}));
    public static final Var const__13 = RT.var((String)"datomic.ddb-s3-cluster", (String)"ensure-config");
    public static final Keyword const__15 = RT.keyword(null, (String)"failed");
    public static final Var const__16 = RT.var((String)"datomic.cli", (String)"fail");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"success"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object p__22804) {
        Object object;
        Object map__22805;
        Object object2;
        Object object3 = p__22804;
        p__22804 = null;
        Object map__228052 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__228052);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__228052;
            map__228052 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__228052;
            map__228052 = null;
        }
        Object storage_config = map__22805 = object2;
        Object object6 = map__22805;
        map__22805 = null;
        Object table_name = RT.get((Object)object6, (Object)const__3);
        try {
            Object object7;
            Object object8 = ((IFn)const__4.getRawRoot()).invoke(storage_config, (Object)const__9);
            if (object8 == null || object8 == Boolean.FALSE) {
                throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke(const__12))));
            }
            Object object9 = storage_config;
            storage_config = null;
            Object result2 = ((IFn)const__13.getRawRoot()).invoke(object9);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object10 = result2;
            Object object11 = iLookupThunk.get(object10);
            if (iLookupThunk == object11) {
                __thunk__0__ = __site__0__.fault(object10);
                object11 = __thunk__0__.get(object10);
            }
            if (object11 != null && object11 != Boolean.FALSE) {
                object7 = result2;
                result2 = null;
            } else {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__15;
                Object object12 = table_name;
                table_name = null;
                objectArray[1] = ((IFn)const__16.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(object12, (Object)" is already configured for ddb+s3 storage. Use '--force true' to overwrite"));
                object7 = RT.mapUniqueKeys((Object[])objectArray);
            }
            object = object7;
        }
        catch (Throwable t2) {
            t2.printStackTrace();
            Object[] objectArray = new Object[2];
            objectArray[0] = const__15;
            Object t2 = null;
            objectArray[1] = ((IFn)const__16.getRawRoot()).invoke((Object)t2.getMessage());
            object = RT.mapUniqueKeys((Object[])objectArray);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb_s3_cluster$ensure_system.invokeStatic(object2);
    }
}

