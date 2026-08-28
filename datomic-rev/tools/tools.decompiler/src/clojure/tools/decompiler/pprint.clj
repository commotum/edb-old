;;   Copyright (c) Nicola Mometto & contributors.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

;; Compatibility replacement for tools.decompiler's obsolete Fipp-based printer.
(ns clojure.tools.decompiler.pprint
  (:require [clojure.pprint :as pp]
            [clojure.string]
            [clojure.walk :as w]))

(defn metadata-preserving-code-dispatch [object]
  ;; clojure.pprint's code-dispatch prints symbol metadata but silently drops
  ;; metadata on vectors. A primitive fn return tag lives on its arity vector,
  ;; so emit that tag explicitly before delegating the vector body.
  (if-let [tag (and (vector? object) (:tag (meta object)))]
    (do
      (print "^")
      (pp/write-out tag)
      (print " ")
      (pp/code-dispatch (with-meta object (dissoc (meta object) :tag))))
    (pp/code-dispatch object)))

(defn ->pprint-str [source]
  (with-out-str
    (binding [pp/*print-right-margin* 100
              *print-meta* true]
      (pp/write source :dispatch metadata-preserving-code-dispatch))))

(defn elide-ns [source]
  (let [!aliases (atom {})
        walk (fn [f s]
               (let [result (w/walk f identity s)]
                 ;; clojure.walk rebuilds collection nodes. Preserve metadata
                 ;; carried by argument vectors, notably primitive fn return
                 ;; tags, across namespace elision and source printing.
                 (if (and (instance? clojure.lang.IMeta s)
                          (instance? clojure.lang.IObj result))
                   (with-meta result (meta s))
                   result)))
        rewrite
        (fn rewrite [x]
          (cond
            (seq? x)
            (if (= 'quote (first x))
              x
              (do
                (when (and (= 'clojure.core/in-ns (first x))
                           (seq? (second x))
                           (= 'quote (-> x second first)))
                  (let [ns-name (-> x second second)
                        ns-name (if (list? ns-name) (second ns-name) ns-name)]
                    ;; Only the most recent in-ns target is the current
                    ;; namespace. Keeping older targets mapped to the empty
                    ;; alias incorrectly removes qualification after a source
                    ;; file switches namespaces more than once.
                    (swap! !aliases
                           (fn [aliases]
                             (->> aliases
                                  (remove (fn [[_ alias]] (= "" alias)))
                                  (into {})
                                  (#(assoc % (name ns-name) "")))))))

                (when (or (= 'clojure.core/refer-clojure (first x))
                          (and (= 'clojure.core/refer (first x))
                               (= '(quote clojure.core) (second x))))
                  (let [excluded (->> x (drop-while (complement #{:exclude})) second)
                        excluded (if (vector? excluded) excluded (rest excluded))]
                    (swap! !aliases assoc "clojure.core"
                           (->> excluded
                                (map (fn [entry]
                                       (str (if (and (seq? entry)
                                                    (= 'quote (first entry)))
                                              (second entry)
                                              entry))))
                                (into #{})))))

                (when (= 'clojure.core/import (first x))
                  (doseq [[_ klass] (rest x)]
                    (swap! !aliases assoc (str klass)
                           (clojure.string/replace (str klass) #".*\.([^.]+)" "$1"))))

                (when (= 'clojure.core/require (first x))
                  (doseq [req (rest x)
                          :when (vector? req)]
                    (when-let [alias (some->> req
                                              (drop-while (complement #{:as}))
                                              second second name)]
                      (let [ns-name (some-> req first second name)]
                        (when-not (= ns-name "clojure.core")
                          (swap! !aliases assoc ns-name alias))))))
                (walk rewrite x)))

            (symbol? x)
            (let [aliases @!aliases]
              (if (= "clojure.core" (namespace x))
                (if (or (contains? #{"in-ns" "with-loading-context" "refer-clojure" "refer"
                                    "require" "use" "import" "list"}
                                   (name x))
                        (some-> (ns-resolve 'clojure.core (symbol (name x))) meta :private)
                        (contains? (get aliases "clojure.core") (name x)))
                  x
                  (symbol (name x)))
                (if-let [alias (get aliases (namespace x))]
                  (if (= "" alias)
                    (symbol (name x))
                    (symbol (str alias "/" (name x))))
                  x)))

            :else
            (walk rewrite x)))]
    (walk rewrite source)))

(defn pprint [source]
  (-> source
      (elide-ns)
      ((fn [form]
         (if (and (seq? form) (= 'do (first form)))
           (list* 'do
                  (->> (rest form)
                       (keep identity)
                       (remove #(or (symbol? %)
                                    (and (seq? %) (= 'var (first %)))
                                    (and (seq? %)
                                         (= 'quote (first %))
                                         (symbol? (second %)))))))
           form)))
      (->pprint-str)))
