(ns imcljs.save
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [imcljs.internal.io :refer [restful]]
            [imcljs.fetch :refer [lists]]
            [cljs.core.async :refer [<!]]))

(defn copy-list-query [{:keys [name type]}]
  {:from   type
   :select [(str type ".id")]
   :where  [{:path  type
             :op    "IN"
             :value name}]})

(defn im-list
  [service name query & [options]]
  (restful :post "/query/tolist" service (merge {:name name :query query :format "json"} options)))

(defn im-list-delete
  [service name & [options]]
  (restful :delete "/lists" service (merge {:name name :format "json"} options)))

(defn im-list-union
  [service name lists & [options]]
  (restful :post "/lists/union" service (merge {:lists lists :name name :format "json"} options)))

(defn im-list-intersect
  [service name lists & [options]]
  (restful :post "/lists/intersect" service (merge {:lists lists :name name :format "json"} options)))

(defn im-list-difference
  [service name lists & [options]]
  (restful :post "/lists/diff" service (merge {:lists lists :name name :format "json"} options)))

(defn im-list-copy
  [service old-list-name new-list-name & [options]]
  (go (<! (im-list service new-list-name (copy-list-query (first (<! (lists service {:name old-list-name}))))))))
