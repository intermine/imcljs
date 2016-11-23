(ns imcljs.internal.utils)

(def does-not-contain? (complement contains?))

(defn one-of? [haystack needle] (some? (some #{needle} haystack)))

(defn missing-http?- [val] (not (re-find #"^https?://" val)))

(defn missing-service?- [val] (not (re-find #"/service$" val)))

(defn append- [text val] (str val text))

(def alphabet (apply sorted-set (clojure.string/split "ABCDEFGHIJKLMNOPQRSTUVWXYZ" "")))

(defn scrub-url
  "Ensures that a url starts with an http protocol and ends with /service"
  [url]
  (cond->> url
           (missing-http?- url) (str "http://")
           (missing-service?- url) (append- "/service")))