/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.reconnector2.Reconnector;
import java.util.Arrays;

public final class reconnector2$reconnector_ref
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"state");
    public static final Keyword const__4 = RT.keyword(null, (String)"reconnect");
    public static final Keyword const__5 = RT.keyword(null, (String)"cleanup");
    public static final Keyword const__6 = RT.keyword(null, (String)"shutdown-state");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__9 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"state"), Symbol.intern(null, (String)"reconnect"), Symbol.intern(null, (String)"cleanup"), Symbol.intern(null, (String)"shutdown-state")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__11 = RT.var((String)"datomic.promise", (String)"delivered");

    public static Object invokeStatic(ISeq p__17146) {
        Object object;
        Object and__5236__auto__17151;
        ISeq iSeq;
        ISeq iSeq2 = p__17146;
        p__17146 = null;
        ISeq map__17147 = iSeq2;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)map__17147);
        if (object2 != null && object2 != Boolean.FALSE) {
            ISeq iSeq3 = map__17147;
            map__17147 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__17147;
            map__17147 = null;
        }
        ISeq map__171472 = iSeq;
        Object state2 = RT.get((Object)map__171472, (Object)const__3);
        Object reconnect = RT.get((Object)map__171472, (Object)const__4);
        Object cleanup2 = RT.get((Object)map__171472, (Object)const__5);
        ISeq iSeq4 = map__171472;
        map__171472 = null;
        Object shutdown_state = RT.get((Object)iSeq4, (Object)const__6);
        Object object3 = and__5236__auto__17151 = state2;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object and__5236__auto__17150;
            Object object4 = and__5236__auto__17150 = reconnect;
            if (object4 != null && object4 != Boolean.FALSE) {
                Object and__5236__auto__17149;
                Object object5 = and__5236__auto__17149 = cleanup2;
                if (object5 != null && object5 != Boolean.FALSE) {
                    object = shutdown_state;
                } else {
                    object = and__5236__auto__17149;
                    and__5236__auto__17149 = null;
                }
            } else {
                object = and__5236__auto__17150;
                and__5236__auto__17150 = null;
            }
        } else {
            object = and__5236__auto__17151;
            and__5236__auto__17151 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__7.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__8.getRawRoot()).invoke(const__9))));
        }
        Object object6 = state2;
        state2 = null;
        Object object7 = shutdown_state;
        shutdown_state = null;
        Object object8 = reconnect;
        reconnect = null;
        Object object9 = cleanup2;
        cleanup2 = null;
        return new Reconnector(((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object6)), ((IFn)const__10.getRawRoot()).invoke(null), object7, object8, object9);
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return reconnector2$reconnector_ref.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

