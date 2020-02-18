(ns imcljs.auth-test
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [async deftest testing is]]
            [cljs.core.async :refer [<!]]
            [imcljs.env :refer [service]]
            [imcljs.fetch :as fetch]
            [imcljs.auth :as auth]))

(deftest anon-token
  (testing "Able to retrieve anon token, even if passed an old/expired token"
    (async done
      (go
        (let [response (<! (fetch/session (assoc service :token "expired")))]
          ;; InterMine returns a 401 if you request a new token and pass an
          ;; old/bad/expired token. We can work around it though!
          (is (not (= 401 (:status response))))
          (done))))))

(deftest basic-auth
  (testing "Basic auth logins the user"
    (async done
      (go
        (let [token (<! (auth/basic-auth service "test_user@mail_account" "secret"))]
          (is (some? (re-matches #"\w+" token)))
          (done))))))

(deftest login
  (testing "Login using the POST endpoint"
    (async done
      (go
        (let [response (<! (auth/login service "test_user@mail_account" "secret"))]
          (is (not-empty response))
          (is (some? (re-matches #"\w+" (get-in response [:output :token]))))
          (done))))))

(deftest logout
  (testing "Logout the user, invalidating their token"
    (async done
      (go
        (let [login-response   (<! (auth/login service "test_user@mail_account" "secret"))
              token            (get-in login-response [:output :token])
              service+auth     (assoc service :token token)
              _logout-response (<! (auth/logout service+auth))
              lists-response   (<! (fetch/lists service+auth))]
          (is (= 401 (:status lists-response)))
          (done))))))

(deftest who-am-i?
  (testing "Show information on the authenticated user"
    (async done
      (go
        (let [login-response  (<! (auth/login service "test_user@mail_account" "secret"))
              token           (get-in login-response [:output :token])
              service+auth    (assoc service :token token)
              whoami-response (<! (auth/who-am-i? service+auth token))]
          (is (map? (:preferences whoami-response)) "Preferences should be a map")
          (is (true? (:superuser whoami-response)) "Superuser flag should be present")
          (done))))))

(deftest change-password
  (testing "Change password of a user"
    (async done
      (go
        (let [login-response      (<! (auth/login service "test_user@mail_account" "secret"))
              token               (get-in login-response [:output :token])
              service+auth        (assoc service :token token)
              change-password-res (<! (auth/change-password service+auth "secret" "overt"))
              login-response-2    (<! (auth/login service "test_user@mail_account" "overt"))
              _                   (<! (auth/change-password service+auth "overt" "secret"))]
          (is (= 200 (:statusCode change-password-res)))
          (is (= 200 (:statusCode login-response-2)))
          (done))))))

(deftest register
  (testing "Register a new user account"
    (async done
      (go
        (let [response (<! (auth/register service "auto_test@intermine.org" "secret"))]
          (is (or (= 400 (:status response)) ; Already registered.
                  (not-empty (:username response))))
          (done))))))

(deftest deregistration-and-delete-account
  (testing "Create a deregistration token and delete the account."
    (async done
      (go
        (let [login-response (<! (auth/login service "auto_test@intermine.org" "secret"))
              token (get-in login-response [:output :token])
              service+auth (assoc service :token token)
              deregistration-token (:uuid (<! (auth/deregistration service+auth)))
              _delete-account (<! (auth/delete-account service+auth deregistration-token))
              login-response-2 (<! (auth/login service "auto_test@intermine.org" "secret"))]
          (is (= 200 (:statusCode login-response)))
          (is (= 401 (:status login-response-2)))
          (done))))))
