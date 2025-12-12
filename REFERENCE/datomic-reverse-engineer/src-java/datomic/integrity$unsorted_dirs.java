/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.btset.IDataSet;
import datomic.index.ITreeIter;
import datomic.integrity$unsorted_dirs$fn__22040;
import datomic.iter.Iter;

public final class integrity$unsorted_dirs
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__3;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object tier, Object sort, Object progress) {
        Object object;
        Iter G__22042;
        Iter iter2;
        Object G__220422;
        Object object2;
        Object G__220423;
        Object object3;
        Object cmp = ((IFn)const__0.getRawRoot()).invoke(sort);
        IFn iFn = (IFn)const__1.getRawRoot();
        Object object4 = progress;
        progress = null;
        Object object5 = cmp;
        cmp = null;
        integrity$unsorted_dirs$fn__22040 integrity$unsorted_dirs$fn__22040 = new integrity$unsorted_dirs$fn__22040(object4, object5);
        Object object6 = db2;
        db2 = null;
        Object G__220424 = object6;
        if (Util.identical((Object)G__220424, null)) {
            object3 = null;
        } else {
            tier = null;
            G__220424 = null;
            object3 = G__220423 = ((IFn)tier).invoke(G__220424);
        }
        if (Util.identical(G__220423, null)) {
            object2 = null;
        } else {
            sort = null;
            G__220423 = null;
            object2 = G__220422 = ((IFn)sort).invoke(G__220423);
        }
        if (Util.identical(G__220422, null)) {
            iter2 = null;
        } else {
            G__220422 = null;
            iter2 = G__22042 = ((IDataSet)G__220422).seek();
        }
        if (Util.identical((Object)G__22042, null)) {
            object = null;
            return iFn.invoke((Object)integrity$unsorted_dirs$fn__22040, object);
        }
        Iter iter3 = G__22042;
        G__22042 = null;
        Iter iter4 = iter3;
        if (Util.classOf((Object)iter3) != __cached_class__0) {
            if (iter4 instanceof ITreeIter) {
                object = ((ITreeIter)((Object)iter4)).dir_seq();
                return iFn.invoke((Object)integrity$unsorted_dirs$fn__22040, object);
            }
            iter4 = iter4;
            __cached_class__0 = Util.classOf((Object)iter4);
        }
        object = const__3.getRawRoot().invoke((Object)iter4);
        return iFn.invoke((Object)integrity$unsorted_dirs$fn__22040, object);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return integrity$unsorted_dirs.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"datomic.integrity", (String)"datom-comparator");
        const__1 = RT.var((String)"datomic.tools", (String)"unsorted-seq");
        const__3 = RT.var((String)"datomic.index", (String)"dir-seq");
    }
}

