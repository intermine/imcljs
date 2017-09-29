(ns imcljs.save
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [imcljs.internal.io :refer [restful]]
            [imcljs.fetch :as fetch :refer [lists]]
            [imcljs.internal.utils :refer [copy-list-query]]
    #?(:cljs [cljs.core.async :refer [<! >! chan]]
       :clj
            [clojure.core.async :refer [<! >! go chan]])))

;(defn copy-list-query [{:keys [name type]}]
;  {:from type
;   :select [(str type ".id")]
;   :where [{:path type
;            :op "IN"
;            :value name}]})


; Indentifiers need to be sent in the body as text/plain. How can we do this with cljs-http?
;(defn im-list-append
;  [service name identifiers-str & [options]]
;  (restful :post "/lists/rename" service (merge {:name name :newname new-name :format "json"} options)))

(defn im-list
  [service name query & [options]]
  (restful :post "/query/tolist" service (merge {:name name :query query :format "json"} options)))

(defn im-list-delete
  [service name & [options]]
  (restful :delete "/lists" service (merge {:name name :format "json"} options)))

(defn im-list-rename
  [service old-name new-name & [options]]
  (restful :post "/lists/rename" service (merge {:oldname old-name :newname new-name :format "json"} options)))

(defn im-list-union
  [service name lists & [options]]
  (restful :post "/lists/union" service (merge {:lists lists :name name :format "json"} options)))

(defn im-list-intersect
  [service name lists & [options]]
  (restful :post "/lists/intersect" service (merge {:lists lists :name name :format "json"} options)))

(defn im-list-difference
  [service name lists & [options]]
  (restful :post "/lists/diff" service (merge {:lists lists :name name :format "json"} options)))

(defn im-list-subtraction
  [service name source-lists subtract-lists & [options]]
  (restful :post "/lists/subtract" service (merge {:references source-lists :subtract subtract-lists :name name :format "json"} options)))

(defn im-list-from-query
  "Save the results of a query to a list"
  [service query name & [options]]
  (restful :post "/query/tolist" service (assoc options :query query :name name)))

(defn im-list-copy
  "Copy a list by name"
  [service old-name new-name & [options]]
  ; Get the details of the old list
  (go (let [old-list-details (<! (fetch/one-list service old-name))]
        ; Create a query from the old list and use it to save the new list
        (<! (im-list-from-query service (copy-list-query old-list-details) new-name)))))
