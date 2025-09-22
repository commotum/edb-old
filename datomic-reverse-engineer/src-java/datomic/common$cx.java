/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.common$cx$alist__9029;
import datomic.common$cx$reify__9026;

public final class common$cx
extends AFunction
implements IFn.OOL {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 136, RT.keyword(null, (String)"column"), 13});
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"cl");

    public static long invokeStatic(Object a, Object b) {
        IObj cmp;
        IObj iObj = cmp = ((IObj)new common$cx$reify__9026(null)).withMeta((IPersistentMap)const__4);
        cmp = null;
        common$cx$alist__9029 alist = new common$cx$alist__9029(iObj);
        Object object = a;
        a = null;
        Object as = ((IFn)alist).invoke(object);
        common$cx$alist__9029 common$cx$alist__9029 = alist;
        alist = null;
        Object object2 = b;
        b = null;
        Object bs = ((IFn)common$cx$alist__9029).invoke(object2);
        Object object3 = as;
        as = null;
        Object object4 = bs;
        bs = null;
        return ((IFn.OOL)const__5.getRawRoot()).invokePrim(object3, object4);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return new Long(common$cx.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$cx.invokeStatic(object3, object4);
    }
}

