(do
  (clojure.core/in-ns 'datomic.tools.locate-transactor)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.pprint :as 'pp]
        ['datomic.api :as 'd]
        ['datomic.coordination :as 'coord]
        ['datomic.peer :as 'peer])))
  (when-not (.equals 'datomic.tools.locate-transactor 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.locate-transactor))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.pprint :as 'pp]
          ['datomic.api :as 'd]
          ['datomic.coordination :as 'coord]
          ['datomic.peer :as 'peer]))))
  (defn print-format
    ([endpoint]
      (update-in
        (dissoc endpoint :password)
        [:timestamp]
        (fn fn__26775 ([n] (when n (java.util.Date. (long ^java.lang.Number n))))))))
  (reset-meta!
    #'print-format
    (assoc
      {:arglists (clojure.core/list ['endpoint]), :column (int 1)}
      :name
      'print-format
      :ns
      *ns*))
  (defn -main
    ([uri]
      (try
        (loop [seq_26779 (seq coord/keys-by-role) chunk_26780 nil count_26781 0 i_26782 0]
          (if (< i_26782 count_26781)
            (let [vec__26783 (.nth ^clojure.lang.Indexed chunk_26780 (int i_26782))
                  role (nth vec__26783 (int 0) nil)
                  k (nth vec__26783 (int 1) nil)]
              (-> (peer/tx-group-endpoint uri k)
               (dissoc :password)
               (assoc :role role)
               (print-format)
               (pp/pprint))
              (recur seq_26779 chunk_26780 count_26781 (inc i_26782)))
            (let [temp__5804__auto__ (seq seq_26779)]
              (when temp__5804__auto__
                (let [seq_26779 temp__5804__auto__]
                  (if (chunked-seq? seq_26779)
                    (let [c__6065__auto__ (chunk-first seq_26779)]
                      (recur
                        (chunk-rest seq_26779)
                        c__6065__auto__
                        (int (count c__6065__auto__))
                        (int 0)))
                    (let [vec__26786 (first seq_26779)
                          role (nth vec__26786 (int 0) nil)
                          k (nth vec__26786 (int 1) nil)]
                      (-> (peer/tx-group-endpoint uri k)
                       (dissoc :password)
                       (assoc :role role)
                       (print-format)
                       (pp/pprint))
                      (recur (next seq_26779) nil 0 0))))))))
        (catch
          java.lang.Throwable
          t
          (do
            (.printStackTrace ^java.lang.Throwable t)
            (d/shutdown true)
            (java.lang.System/exit (int -1))
            nil)))
      (d/shutdown true)
      (java.lang.System/exit (int 0))
      nil))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name '-main :ns *ns*)))