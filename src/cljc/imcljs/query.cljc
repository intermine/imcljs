(ns imcljs.query
  (:require [imcljs.path :as path]
            [clojure.string :refer [join blank? escape]]
            [clojure.set :refer [difference rename-keys]]
            [imcljs.internal.utils :refer [alphabet]]))

(defn value [x] (str "<value>" x "</value>"))

(defn rename-key [m old-k new-k]
  (-> m (assoc new-k (get m old-k)) (dissoc old-k)))

(defn add-id [s]
  (if (= ".id" (subs s (- (count s) (count ".id"))))
    s
    (str s ".id")))

(defn ids->constraint [c]
  (-> c
      (rename-key :ids :values)
      (assoc :op "ONE OF")
      (update :path add-id)))

(def html-entities {"<" "&lt;"
                    "<=" "&lt;="
                    ">" "&gt;"
                    ">=" "&gt;="})

(let [cmap {\" "&quot;"
            \> "&gt;"
            \< "&lt;"
            \& "&amp;"}]
  (defn escape-attribute
    "Replace double quotation mark, angle brackets and ampersand characters with
    their HTML entity, as these are not permitted in XML attribute values."
    [s]
    (escape s cmap)))

(defn map->xmlstr
  "xml string representation of an edn map.
  (map->xlmstr constraint {:key1 val1 key2 val2}) => <constraint key1=val1 key2=val2 />"
  [elem m]
  (let [m      (cond-> m (contains? m :ids) ids->constraint)
        m      (select-keys m [:path :value :values :type :op :code :description :editable :switchable])
        values (:values m)]

    (str "\n   <" elem " "
         (reduce (fn [total [k v]]
                   (if (not= k :values)
                     (str total (when total " ") (name k)
                          "="
                          (str \" (-> v str escape-attribute) \"))
                     total))
                 nil m)
         (if values
           (str ">" (apply str (map value values)) "</" elem ">")
           "/>"))))

(defn stringify-map
  [m]
  (reduce (fn [total [k v]]
            (str total (when total " ") (name k) "=" (str \" (-> v str escape-attribute) \")))
          nil
          m))

(defn enforce-origin [query]
  (if (nil? (:from query))
    (assoc query :from (first (clojure.string/split (first (:select query)) #"\.")))
    query))

(defn enforce-views-have-class [query]
  (update query :select
          (partial mapv
                   (fn [path]
                     (let [path (name path)]
                       (if (= (:from query) (first (clojure.string/split path #"\.")))
                         path
                         (str (:from query) "." path)))))))

(defn enforce-joins-have-class [query]
  (if (contains? query :joins)
    (update query :joins
            (partial mapv
                     (fn [path]
                       (let [path (name path)]
                         (if (= (:from query) (first (clojure.string/split path #"\.")))
                           path
                           (str (:from query) "." path))))))
    query))

(defn enforce-constraints-have-class [query]
  (if (contains? query :where)
    (update query :where
            (partial mapv
                     (fn [constraint]
                       (let [path (:path constraint)]
                         (if (= (:from query) (first (clojure.string/split path #"\.")))
                           constraint
                           (assoc constraint :path (str (:from query) "." path)))))))
    query))

(defn enforce-constraints-have-code [query]
  (if (contains? query :where)
    (update query :where
            (fn [constraints]
              (reduce (fn [total {:keys [code type] :as constraint}]
                        (if (or (some? code)
                                ;; Type (aka subclass) constraints may not
                                ;; participate in the constraint logic.
                                (some? type))
                          (conj total constraint)
                          (let [existing-codes      (set (remove nil? (concat (map :code constraints) (map :code total))))
                                next-available-code (first (filter (complement blank?) (difference alphabet existing-codes)))]
                            (conj total (assoc constraint :code next-available-code))))) [] constraints)))
    query))

(defn enforce-constraints-loop-as-value [query]
  (if (contains? query :where)
    (update query :where
            (partial mapv
                     (fn [{:keys [loopPath value] :as constraint}]
                       (if (and (some? loopPath) (nil? value))
                         (-> constraint
                             (assoc :value (if (= (:from query) (first (clojure.string/split loopPath #"\.")))
                                             loopPath
                                             (str (:from query) "." loopPath)))
                             (dissoc :loopPath))
                         constraint))))
    query))

(defn enforce-constraints-valueless
  "IS NULL and IS NOT NULL constraints shouldn't have a value."
  [query]
  (if (contains? query :where)
    (update query :where
            (partial mapv
                     (fn [{:keys [op] :as constraint}]
                       (if (contains? #{"IS NULL" "IS NOT NULL"} op)
                         (cond-> constraint
                           (contains? constraint :value) (dissoc :value)
                           (contains? constraint :values) (dissoc :values))
                         constraint))))
    query))

(defn enforce-sort-order
  "Makes sure the query XML will have a sortOrder attribute instead of orderBy.
  Only the former is supported as part of the PathQuery API."
  [query]
  (if (and (empty? (:sortOrder query))
           (contains? query :orderBy))
    (rename-keys query {:orderBy :sortOrder})
    query))

(defn enforce-sorting [query]
  (if (contains? query :sortOrder)
    (update query :sortOrder
            (partial mapv
                     (fn [order]
                       (let [order (if (nil? (:path order))
                                     {:path (str (name (first (first (seq order)))))
                                      :direction (second (first (seq order)))}
                                     order)]
                         (if (= (:from query) (first (clojure.string/split (:path order) #"\.")))
                           order
                           (assoc order :path (str (:from query) "." (:path order))))))))

    query))

(def sterilize-query (comp
                      enforce-sorting
                      enforce-sort-order
                      enforce-constraints-valueless
                      enforce-constraints-loop-as-value
                      enforce-constraints-have-class
                      enforce-constraints-have-code
                      enforce-joins-have-class
                      enforce-views-have-class
                      enforce-origin))

(defn make-join [join-path] (str "\n  <join path=\"" join-path "\" style=\"OUTER\"/>"))

(defn ->xml
  "Returns the stringfied XML representation of an EDN intermine query."
  [model query]
  ;(if (nil query) (throw (js/Error. "Oops!")))

  (let [query           (sterilize-query query)
        head-attributes (cond-> {:model (:name model)
                                 :view (clojure.string/join " " (:select query))}
                          (:constraintLogic query) (assoc :constraintLogic (:constraintLogic query))
                          (:sortOrder query) (assoc :sortOrder (clojure.string/join " " (flatten (map (juxt :path :direction) (:sortOrder query)))))
                          (:title query) (assoc :name (:title query))
                          (:longDescription query) (assoc :longDescription (:longDescription query)))]
    (str "<query " (stringify-map head-attributes) ">"
         (when (:joins query) (apply str (map make-join (:joins query))))

         (apply str (map (partial map->xmlstr "constraint") (:where query)))
         "\n</query>")))

(defn- substitute-constraints
  "Inner joins are default for every class in the view, which means once
  they're removed from the view, we need their equivalent as constraints to
  query the same set of objects. This doesn't apply to outer joins and the only
  selected path, which won't get added as constraints."
  [model query select-path]
  (let [joins (conj (set (:joins query)) select-path)]
    (map (fn [class-path]
           {:path (str class-path ".id")
            :op "IS NOT NULL"})
         (into #{}
               (comp (map #(path/trim-to-last-class model %))
                     (remove #(contains? joins %)))
               (:select query)))))

(defn deconstruct-by-class
  "Deconstructs a query by its views and groups them by class.
  (deconstruct-by-class model query)
  {:Gene {Gene.homologues.homologue {:from Gene :select [Gene.homologues.homologue.id] :where [...]}
         {Gene {:from Gene :select [Gene.id] :where [...]}}}
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `imcljs.path/walk` for more information)."
  [model query]
  (let [query (sterilize-query query)]
    (reduce (fn [path-map next-path]
              (let [select-path (path/trim-to-last-class model next-path)]
                (update path-map (path/class model next-path)
                        assoc (path/trim-to-last-class model next-path)
                        {:query (-> query
                                    (assoc :select [(str select-path ".id")])
                                    (update :where into (substitute-constraints model query select-path)))})))
            {} (:select query))))

(defn group-views-by-class
  "Group the views of a query by their Class and provide a query
  to retrieve just that column of data.
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `imcljs.path/walk` for more information)."
  [model query]
  (let [query (sterilize-query query)]
    (reduce (fn [path-map next-path]
              (let [select-path (path/trim-to-last-class model next-path)]
                (update path-map (path/class model next-path)
                        (comp vec set conj)
                        {:path (str (path/trim-to-last-class model next-path) ".id")
                         :type (path/class model next-path)
                         :query (-> query
                                    (assoc :select [(str select-path ".id")])
                                    (update :where into (substitute-constraints model query select-path)))})))
            {} (:select query))))

