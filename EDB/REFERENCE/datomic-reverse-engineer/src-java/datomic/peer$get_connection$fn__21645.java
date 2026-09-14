/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.peer$get_connection$fn__21645$fn__21647;

public final class peer$get_connection$fn__21645
extends AFunction {
    Object cluster_conf;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.coordination", (String)"resolve-db-name");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"db-id");
    public static final Var const__6 = RT.var((String)"datomic.peer", (String)"connection-lock");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__9 = RT.keyword(null, (String)"threw");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public peer$get_connection$fn__21645(Object object) {
        this.cluster_conf = object;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object map__21646;
            Object object;
            Object temp__5455__auto__21652;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object object2 = temp__5455__auto__21652 = ((IFn)const__1.getRawRoot()).invoke(this.cluster_conf);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = temp__5455__auto__21652;
                temp__5455__auto__21652 = null;
                Object map__216462 = object3;
                Object object4 = ((IFn)const__2.getRawRoot()).invoke(map__216462);
                if (object4 != null && object4 != Boolean.FALSE) {
                    Object object5 = map__216462;
                    map__216462 = null;
                    object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object5)));
                } else {
                    object = map__216462;
                    map__216462 = null;
                }
            } else {
                IFn iFn = (IFn)const__7.getRawRoot();
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object6 = this.cluster_conf;
                this.cluster_conf = null;
                Object object7 = iLookupThunk.get(object6);
                if (iLookupThunk == object7) {
                    __thunk__0__ = __site__0__.fault(object6);
                    object7 = __thunk__0__.get(object6);
                }
                throw (Throwable)new RuntimeException((String)iFn.invoke((Object)"Could not find ", object7, (Object)" in catalog"));
            }
            Object resolved_cluster_conf = map__21646 = object;
            Object object8 = map__21646;
            map__21646 = null;
            RT.get((Object)object8, (Object)const__5);
            Object lockee__5436__auto__21651 = const__6.getRawRoot();
            Object object9 = resolved_cluster_conf;
            resolved_cluster_conf = null;
            Object object10 = lockee__5436__auto__21651;
            lockee__5436__auto__21651 = null;
            objectArray[1] = ((IFn)new peer$get_connection$fn__21645$fn__21647(object9, object10)).invoke();
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__9;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

