/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  org.fressian.Reader
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import org.fressian.Reader;

public final class fressian$defressian
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"handlers");
    public static final Keyword const__4 = RT.keyword(null, (String)"footer");
    public static final Var const__5 = RT.var((String)"datomic.fressian", (String)"create-reader");

    public static Object invokeStatic(Object in, ISeq p__12176) {
        ISeq iSeq;
        ISeq iSeq2 = p__12176;
        p__12176 = null;
        ISeq map__12177 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__12177);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__12177;
            map__12177 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__12177;
            map__12177 = null;
        }
        ISeq map__121772 = iSeq;
        Object handlers = RT.get((Object)map__121772, (Object)const__3);
        ISeq iSeq4 = map__121772;
        map__121772 = null;
        Object footer = RT.get((Object)iSeq4, (Object)const__4);
        Object object2 = in;
        in = null;
        Object object3 = handlers;
        handlers = null;
        Object fin = ((IFn)const__5.getRawRoot()).invoke(object2, object3, (Object)(RT.booleanCast((Object)footer) ? Boolean.TRUE : Boolean.FALSE));
        Object result2 = ((Reader)fin).readObject();
        Object object4 = footer;
        footer = null;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = fin;
            fin = null;
            ((Reader)object5).validateFooter();
        }
        Object object6 = result2;
        result2 = null;
        return object6;
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return fressian$defressian.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

