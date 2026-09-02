(do
  (clojure.core/in-ns 'datomic.query.support)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.query.support)
    {:doc
     "Shared query-form normalization and result adapters. Accepts EDN strings, sequential query forms, and query maps; handles :keys, :strs, and :syms return-map specifications; preserves find order in indexed return maps; and provides counted lazy query results."})
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.edn :as 'edn])))
  (when-not (.equals 'datomic.query.support 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.query.support))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.edn :as 'edn]))))
  (defn incorrect!
    ([msg]
      (throw
        (ex-info
          msg
          #:cognitect.anomalies{:category :cognitect.anomalies/incorrect, :message msg}))))
  (reset-meta!
    #'incorrect!
    (assoc
      {:private true, :arglists (clojure.core/list ['msg]), :column (int 1)}
      :name
      'incorrect!
      :ns
      *ns*))
  (defn listq->mapq
    ([lq]
      (reduce
        (fn fn__18851
          ([m p__18850]
            (let [vec__18852 p__18850
                  k (nth vec__18852 (int 0) nil)
                  v (nth vec__18852 (int 1) nil)
                  k (first k)]
              (assoc m k v))))
        {}
        (partition 2 (partition-by #{:find :where :syms :keys :with :timeout :strs :in} lq)))))
  (reset-meta!
    #'listq->mapq
    (assoc
      {:arglists (clojure.core/list ['lq]),
       :doc
       "Converts a sequential query form into a map keyed by :find, :with, :in, :where, :timeout, and return-map clauses. Clause values retain their original order.",
       :column (int 1)}
      :name
      'listq->mapq
      :ns
      *ns*))
  (defn disallow-find-variants!
    ([query]
      (when (some
              (fn fn__18858 ([p1__18857#] (or (vector? p1__18857#) (= '. p1__18857#))))
              (:find query))
        (incorrect! "Only find-rel elements are allowed in client :find"))))
  (reset-meta!
    #'disallow-find-variants!
    (assoc
      {:arglists (clojure.core/list ['query]), :column (int 1)}
      :doc
      "Rejects scalar, collection, and tuple find specifications where only relation results are supported. Throws an incorrect-input anomaly."
      :name
      'disallow-find-variants!
      :ns
      *ns*))
  (defn query-map
    ([q] (let [q (if (string? q) (edn/read-string q) q)] (if (sequential? q) (listq->mapq q) q))))
  (reset-meta!
    #'query-map
    (assoc
      {:arglists (clojure.core/list ['q]),
       :doc
       "Returns q as a query map. EDN strings are read first, sequential forms are partitioned into clauses, and maps are returned unchanged.",
       :column (int 1)}
      :name
      'query-map
      :ns
      *ns*))
  (defn parse-as
    ([q]
      (let [map__18863 (query-map q)
            map__18863 (if (seq? map__18863)
                         (if (next map__18863)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__18863))
                           (if (seq map__18863) (first map__18863) {}))
                         map__18863)
            nq map__18863
            find (get map__18863 :find)
            keys (get map__18863 :keys)
            strs (get map__18863 :strs)
            syms (get map__18863 :syms)
            temp__5802__auto__ (or keys strs syms)]
        (if temp__5802__auto__
          (let [asyms temp__5802__auto__
                as (let [G__18864 asyms G__18864 (if keys (map keyword G__18864) G__18864)]
                     (cond-> (if strs (map str G__18864) G__18864) :then (vec)))]
            (when-not (= (count (or keys strs syms)) (count find))
              (throw (incorrect! "Count of :keys/:strs/:syms must match count of :find")))
            [(dissoc nq :keys :syms :strs) as])
          [nq nil]))))
  (reset-meta!
    #'parse-as
    (assoc
      {:arglists (clojure.core/list ['q]),
       :doc
       "Returns [query-map result-keys]. Converts :keys names to keywords, :strs names to strings, and :syms names to symbols, then removes the return-map clause from the query. The number of names must equal the number of :find elements.",
       :column (int 1)}
      :name
      'parse-as
      :ns
      *ns*))
  (defn counted-seq
    ([base-seq ct meta]
      (when-not (< ct 1)
        (proxy
          [clojure.lang.ASeq clojure.lang.Counted]
          [^clojure.lang.IPersistentMap meta]
          (more [] (rest base-seq))
          (seq [] base-seq)
          (next [] (next base-seq))
          (contains [o] (.contains ^java.util.List base-seq o))
          (count [] ct)
          (listIterator
            ([] (.listIterator ^java.util.List base-seq))
            ([index] (.listIterator ^java.util.List base-seq (int ^java.lang.Number index))))
          (cons [o] (cons o base-seq))
          (iterator [] (.iterator ^java.util.List base-seq))
          (subList
            [from to]
            (.subList
              ^java.util.List base-seq
              (int ^java.lang.Number from)
              (int ^java.lang.Number to)))
          (lastIndexOf
            [o]
            (java.lang.Integer/valueOf (int (.lastIndexOf ^java.util.List base-seq o))))
          (withMeta [meta] (counted-seq base-seq ct meta))
          (hashCode [] (java.lang.Integer/valueOf (int (.hashCode base-seq))))
          (hasheq [] (hash base-seq))
          (indexOf [o] (java.lang.Integer/valueOf (int (.indexOf ^java.util.List base-seq o))))
          (toArray
            ([] (.toArray ^java.util.List base-seq))
            ([o] (.toArray ^java.util.List base-seq ^"[Ljava.lang.Object;" o)))
          (get [index] (nth base-seq (int ^java.lang.Number index)))
          (equals [o] (.equals base-seq o))
          (equiv [o] (= base-seq o))
          (containsAll [c] (.containsAll ^java.util.List base-seq ^java.util.Collection c))
          (first [] (first base-seq)))))
    ([base-seq ct] (counted-seq base-seq ct nil)))
  (reset-meta!
    #'counted-seq
    (assoc
      {:arglists
       (clojure.core/list
         ['base-seq 'ct]
         [(.withMeta 'base-seq {:tag 'java.util.List}) 'ct 'meta]),
       :doc
       "Wraps base-seq in an ASeq whose Counted implementation returns ct without realizing the sequence. Returns nil when ct is less than one.",
       :column (int 1)}
      :name
      'counted-seq
      :ns
      *ns*)))
