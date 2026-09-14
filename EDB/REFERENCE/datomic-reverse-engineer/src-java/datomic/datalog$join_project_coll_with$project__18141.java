/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$join_project_coll_with$project__18141
extends AFunction {
    Object px_from;
    Object py_to;
    Object py_from;
    int proj_count;
    Object px_to;
    public static final Var const__10 = RT.var((String)"datomic.datalog", (String)"tuple");

    public datalog$join_project_coll_with$project__18141(Object object, Object object2, Object object3, int n, Object object4) {
        this.px_from = object;
        this.py_to = object2;
        this.py_from = object3;
        this.proj_count = n;
        this.px_to = object4;
    }

    public Object invoke(Object x, Object y) {
        long i;
        Object[] ret = RT.object_array((Object)this_.proj_count);
        long n__5742__auto__18143 = ((Object[])this_.px_from).length;
        for (i = 0L; i < n__5742__auto__18143; ++i) {
            RT.aset((Object[])ret, (int)RT.uncheckedIntCast((Object)RT.aget((Object[])((Object[])this_.px_to), (int)((int)i))), (Object)RT.nth((Object)x, (int)RT.uncheckedIntCast((Object)((Number)RT.aget((Object[])((Object[])this_.px_from), (int)((int)i))))));
        }
        long n__5742__auto__18144 = ((Object[])this_.py_from).length;
        for (i = 0L; i < n__5742__auto__18144; ++i) {
            RT.aset((Object[])ret, (int)RT.uncheckedIntCast((Object)RT.aget((Object[])((Object[])this_.py_to), (int)((int)i))), (Object)RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)RT.aget((Object[])((Object[])this_.py_from), (int)((int)i))))));
        }
        Object[] objectArray = ret;
        ret = null;
        datalog$join_project_coll_with$project__18141 this_ = null;
        return ((IFn)const__10.getRawRoot()).invoke((Object)objectArray);
    }
}

