(ns imcljs.internal.utils
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:require [clojure.string :refer [split]]
    #?(:cljs [cljs.core.async :as a :refer [<! >! chan]]
       :clj
            [clojure.core.async :as a :refer [<! >! go go-loop chan]])
    #?(:clj
            [clojure.data.codec.base64 :as b64])))




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

(defn <<!
  "Given a collection of channels, returns a collection containing
  the first result of each channel (similiar to JS Promise.all)"
  [chans]
  (go-loop [coll '()
            chans chans]
    (if (seq chans)
      (recur (conj coll (<! (first chans)))
             (rest chans))
      coll)))

(def base64 #?(:cljs js/btoa
               :clj (fn [s] (String. (b64/encode (.getBytes s)) "UTF-8"))))