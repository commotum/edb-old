(do
  (clojure.core/in-ns 'datomic.garbage.fressian)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.fressian :as 'fressian])
      (clojure.core/import 'org.fressian.handlers.ReadHandler)
      (clojure.core/import 'org.fressian.handlers.WriteHandler)))
  (when-not (.equals 'datomic.garbage.fressian 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.garbage.fressian))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.fressian :as 'fressian])
        (clojure.core/import 'org.fressian.handlers.ReadHandler)
        (clojure.core/import 'org.fressian.handlers.WriteHandler))))
  (declare ->GarbageRoot)
  (declare map->GarbageRoot)
  (defrecord GarbageRoot [children])
  (clojure.core/import 'datomic.garbage.fressian.GarbageRoot)
  (defn ->GarbageRoot ([children] (datomic.garbage.fressian.GarbageRoot. children)))
  (defn map->GarbageRoot
    ([m__7585__auto__]
      (GarbageRoot/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (declare ->GarbageDir)
  (declare map->GarbageDir)
  (defrecord GarbageDir [children])
  (clojure.core/import 'datomic.garbage.fressian.GarbageDir)
  (defn ->GarbageDir ([children] (datomic.garbage.fressian.GarbageDir. children)))
  (defn map->GarbageDir
    ([m__7585__auto__]
      (GarbageDir/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (declare ->GarbageLeaf)
  (declare map->GarbageLeaf)
  (defrecord GarbageLeaf [children])
  (clojure.core/import 'datomic.garbage.fressian.GarbageLeaf)
  (defn ->GarbageLeaf ([children] (datomic.garbage.fressian.GarbageLeaf. children)))
  (defn map->GarbageLeaf
    ([m__7585__auto__]
      (GarbageLeaf/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn write-handler
    ([tag]
      (reify
        org.fressian.handlers.WriteHandler
        (^void write
          [this ^org.fressian.Writer w o]
          (do
            (.writeTag ^org.fressian.Writer w tag (int 1))
            (.writeObject ^org.fressian.Writer w (:children o))
            nil)))))
  (reset-meta!
    #'write-handler
    (assoc
      {:private true, :arglists (clojure.core/list ['tag]), :column 1}
      :name
      'write-handler
      :ns
      *ns*))
  (defn read-handler
    ([factory]
      (reify
        org.fressian.handlers.ReadHandler
        (read
          [this ^org.fressian.Reader rdr tag ^int component_count]
          (^clojure.lang.IFn factory (into [] (.readObject ^org.fressian.Reader rdr)))))))
  (reset-meta!
    #'read-handler
    (assoc
      {:private true, :arglists (clojure.core/list ['factory]), :column 1}
      :name
      'read-handler
      :ns
      *ns*))
  (def write-handlers
   (assoc
     fressian/user-write-handlers
     datomic.garbage.fressian.GarbageRoot
     {"garbage-root" (write-handler "garbage-root")}
     datomic.garbage.fressian.GarbageDir
     {"garbage-dir" (write-handler "garbage-dir")}
     datomic.garbage.fressian.GarbageLeaf
     {"garbage-leaf" (write-handler "garbage-leaf")}
     clojure.lang.PersistentVector
     {"vec"
      (reify
        org.fressian.handlers.WriteHandler
        (^void write
          [this ^org.fressian.Writer w o]
          (do
            (.writeTag ^org.fressian.Writer w "vec" (int 1))
            (.writeObject ^org.fressian.Writer w (seq o))
            nil)))}))
  (def read-handlers
   (assoc
     fressian/user-read-handlers
     "garbage-root"
     (read-handler ->GarbageRoot)
     "garbage-dir"
     (read-handler ->GarbageDir)
     "garbage-leaf"
     (read-handler ->GarbageLeaf)
     "vec"
     (reify
       org.fressian.handlers.ReadHandler
       (read
         [this ^org.fressian.Reader rdr _ ^int _]
         (let [os (.readObject ^org.fressian.Reader rdr)]
           (clojure.lang.PersistentVector/create (seq os))))))))