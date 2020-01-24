(ns imcljs.save
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [imcljs.internal.io :refer [restful]]
            [imcljs.fetch :as fetch :refer [lists]]
            [imcljs.internal.utils :refer [copy-list-query <<!]]
            #?(:cljs [cljs.core.async :as a :refer [<! >! chan]]
               :clj
               [clojure.core.async :as a :refer [<! >! go chan]])))

(defn im-list
  "Creates a list using a plain text string of identifiers"
  [service name type identifiers & [options]]
  (restful :post-body (str "/lists?name=" name "&type=" type) service {:body identifiers :headers {"Content-Type" "text/plain"}}))

(defn im-list-update
  "Update an existing list. Currently, only updating the description by specifying
  `:newDescription` in the `options` map is supported."
  [service name & [options]]
  (restful :put "/lists" service (merge {:name name :format "json"} options)))

(defn im-list-delete
  "Delete one or name lists."
  [service names & [options]]
  (if (coll? names)
    (<<! (map (fn [name] (restful :delete "/lists" service (merge {:name name :format "json"} options))) names))
    (restful :delete "/lists" service (merge {:name names :format "json"} options))))

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
  [service name query & [options]]
  (restful :post "/query/tolist" service (merge {:name name :query query :format "json"} options)))

(defn im-list-add-tag
  [service name tags & [options]]
  (restful :post "/list/tags" service (merge {:name name
                                              :tags (if (coll? tags) (interpose ";" tags) tags)
                                              :format "json"}
                                             options)))

(defn im-list-remove-tag
  [service name tags & [options]]
  (restful :delete "/list/tags" service (merge {:name name
                                                :tags (if (coll? tags) (interpose ";" tags) tags)
                                                :format "json"}
                                               options)))

(defn im-list-copy
  "Copy a list by name"
  [service old-name new-name & [options]]
  ; Get the details of the old list
  (go (let [old-list-details (<! (fetch/one-list service old-name))]
        ; Create a query from the old list and use it to save the new list
        (<! (im-list-from-query service new-name (copy-list-query old-list-details))))))

(defn preferences
  "Set the preferences for the authenticated user by passing a map.
  Note that none of the values can be an empty string. For that you'll have to
  use `delete-preference` instead."
  [service preferences & [options]]
  (restful :post "/user/preferences" service (merge preferences options) :preferences))

(defn delete-preference
  "Delete a single stored preference by key for the authenticated user."
  [service preference & [options]]
  (let [params (merge {:key preference} options)]
    (restful :delete "/user/preferences" service params :preferences)))
