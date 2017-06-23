(ns imcljs.auth
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [imcljs.internal.io :refer [restful]]
    #?(:cljs [cljs.core.async :refer [<! >! chan]]
       :clj [clojure.core.async :refer [<! >! go chan]])))

(defn basic-auth
  "Given a username and a password return an API token"
  [service username password]
  (restful :basic-auth "/user/token" service {:username username :password password} :token))

(defn who-am-i?
  "Given a token return user information"
  [service token & [options]]
  (restful :get "/user/whoami" service (merge {:token token} options) :user))