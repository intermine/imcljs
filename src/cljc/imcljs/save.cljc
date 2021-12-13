(ns imcljs.save
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [imcljs.internal.io :refer [restful]]
            [imcljs.fetch :as fetch :refer [lists]]
            [imcljs.internal.utils :refer [copy-list-query <<!]]
            [clojure.string :refer [join blank?]]
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
                                              :tags (if (coll? tags) (join ";" tags) tags)
                                              :format "json"}
                                             options)))

(defn im-list-remove-tag
  [service name tags & [options]]
  (restful :delete "/list/tags" service (merge {:name name
                                                :tags (if (coll? tags) (join ";" tags) tags)
                                                :format "json"}
                                               options)))

(defn im-list-copy
  "Copy a list by name"
  [service old-name new-name & [options]]
  ; Get the details of the old list
  (go (let [old-list-details (<! (fetch/one-list service old-name))]
        ; Create a query from the old list and use it to save the new list
        (<! (im-list-from-query service new-name (copy-list-query old-list-details))))))

(defn im-list-upgrade
  "Perform a list upgrade, replacing it with the specified up-to-date object IDs."
  [service name object-ids]
  (restful :post-body (str "/upgradelist?name=" name) service
           {:body (join ", " object-ids)
            :headers {"Content-Type" "text/plain"}}))

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

(defn query
  "Upload a query to be saved into the user's profile.
  As of InterMine 4.1.2, the webservice returns an invalid JSON response, hence
  why we use `:format 'text'` and no xform."
  [service query & [options]]
  (let [params (merge {:query query :format "text"} options)]
    (restful :post "/user/queries" service params)))

(defn delete-query
  "Delete a query that has previously been saved into the user's profile."
  [service title & [options]]
  (let [params (merge {:name title} options)]
    (restful :delete "/user/queries" service params)))

(defn bluegenes-properties
  "Add a new key to the BlueGenes-specific config for a mine.
  Requires that you are authenticated as an admin."
  [service key value & [options]]
  (let [params (merge {:key key :value value} options)]
    (restful :post "/bluegenes-properties" service params)))

(defn update-bluegenes-properties
  "Update an existing key in the BlueGenes-specific config for a mine.
  Requires that you are authenticated as an admin."
  [service key value & [options]]
  (let [params (merge {:key key :value value} options)]
    (restful :put-body "/bluegenes-properties" service params)))

(defn delete-bluegenes-properties
  "Delete an existing key in the BlueGenes-specific config for a mine.
  Requires that you are authenticated as an admin."
  [service key & [options]]
  (let [params (merge {:key key} options)]
    (restful :delete "/bluegenes-properties" service params)))

(defn feedback
  "Uses the mine's email service to send an email to the maintainers.
  Email is optional and will be excluded if nil or empty."
  [service email feedback & [options]]
  (let [params (cond-> {:feedback feedback}
                 (not (blank? email)) (assoc :email email))]
    (restful :post "/feedback" service (merge params options))))

(defn template
  "Save a template, or overwrite an existing with the same name.
  Takes one or more templates each containing a query, serialised in XML or JSON."
  [service template-query & [options]]
  (let [params (merge {:query template-query} options)]
    (restful :post "/template/upload" service params)))

(defn delete-template
  "Delete a template by name."
  [service template-name & [options]]
  (let [params (merge {:name template-name} options)]
    (restful :delete (str "/templates/" template-name) service params)))

(defn precompute
  "Precompute a template by name. Must be owned by superuser."
  [service template-name & [options]]
  (let [params (merge {:name template-name} options)]
    (restful :post "/template/precompute" service params :templates)))

(defn summarise
  "Summarise a template by name. Must be owned by superuser."
  [service template-name & [options]]
  (let [params (merge {:name template-name} options)]
    (restful :post "/template/summarise" service params :templates)))

(defn template-add-tags
  "Add one or more tags to a template."
  [service template-name tags & [options]]
  (let [params (merge {:name template-name
                       :tags (if (coll? tags) (join ";" tags) tags)}
                      options)]
    (restful :post "/template/tags" service params)))

(defn template-remove-tags
  "Delete one or more tags from a template."
  [service template-name tags & [options]]
  (let [params (merge {:name template-name
                       :tags (if (coll? tags) (join ";" tags) tags)}
                      options)]
    (restful :delete "/template/tags" service params)))
