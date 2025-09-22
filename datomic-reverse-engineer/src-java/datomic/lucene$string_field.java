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
 *  com.datomic.lucene.document.Field
 *  com.datomic.lucene.document.Field$Index
 *  com.datomic.lucene.document.Field$Store
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import com.datomic.lucene.document.Field;

public final class lucene$string_field
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"index");
    public static final Keyword const__4 = RT.keyword(null, (String)"store");
    public static final Keyword const__5 = RT.keyword(null, (String)"analyze");
    public static final Keyword const__6 = RT.keyword(null, (String)"omit-norms");

    public static Object invokeStatic(Object name, Object value, ISeq p__12245) {
        ISeq iSeq;
        ISeq iSeq2 = p__12245;
        p__12245 = null;
        ISeq map__12246 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__12246);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__12246;
            map__12246 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__12246;
            map__12246 = null;
        }
        ISeq map__122462 = iSeq;
        Object index2 = RT.get((Object)map__122462, (Object)const__3);
        Object store = RT.get((Object)map__122462, (Object)const__4);
        Object analyze = RT.get((Object)map__122462, (Object)const__5);
        ISeq iSeq4 = map__122462;
        map__122462 = null;
        Object omit_norms = RT.get((Object)iSeq4, (Object)const__6);
        Object object2 = name;
        name = null;
        Object object3 = value;
        value = null;
        Object object4 = store;
        store = null;
        Object object5 = index2;
        index2 = null;
        Object object6 = analyze;
        analyze = null;
        Object object7 = omit_norms;
        omit_norms = null;
        return new Field((String)object2, (String)object3, object4 != null && object4 != Boolean.FALSE ? Field.Store.YES : Field.Store.NO, Field.Index.toIndex((boolean)RT.booleanCast((Object)object5), (boolean)RT.booleanCast((Object)object6), (boolean)RT.booleanCast((Object)object7)));
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return lucene$string_field.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

