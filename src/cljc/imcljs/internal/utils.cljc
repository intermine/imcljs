(ns imcljs.internal.utils
  (:require [clojure.string :refer [split]]))

(def does-not-contain? (complement contains?))

(defn one-of? [haystack needle] (some? (some #{needle} haystack)))

(defn missing-http?- [val] (not (re-find #"^https?://" val)))

(defn missing-service?- [val] (not (re-find #"/service$" val)))

(defn append- [text val] (str val text))

(def alphabet (apply sorted-set (split "ABCDEFGHIJKLMNOPQRSTUVWXYZ" #"")))

(defn scrub-url
  "Ensures that a url starts with an http protocol and ends with /service"
  [url]
  (cond->> url
           (missing-http?- url) (str "http://")
           (missing-service?- url) (append- "/service")))

(defn copy-list-query
  [{old-list-name :name old-list-type :type :as old-list-details}]
  {:from old-list-type
   :select [(str old-list-type ".id")]
   :where [{:path old-list-type :op "IN" :value old-list-name}]})