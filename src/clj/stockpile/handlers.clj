(ns stockpile.handlers
  (:require
   [stockpile.db :as db]
   [ring.util.response :as resp]))

(defn timeout
  [_]
  (resp/redirect "/" 403))

;; User handlers
(defn register
  [{:keys [parameters]}]
  (let [data (:body parameters)]
    (if (db/email-taken? (:email data))
      {:status 401,
       :body {:error "This email is taken"}}
      (do (db/create-user! data)
          {:status 201,
           :body
           {:success
            (str "user "
                 (:username data)
                 " was created succesfully")}}))))

(defn login
  [{:keys [parameters]}]
  (let [data (:body parameters)
        user (db/get-user-by-credentials data)]
    (if (nil? user)
      {:status 401,
       :body {:error "wrong email or password"}}
      {:status 200,
       :session (select-keys (into {} user)
                             [:username :email
                              :role]),
       :body {:user user}})))


(defn logout
  [_]
  {:status 200, :session nil})

(defn users
  [_]
  {:status 200,
   :body {:users (db/get-all-users)}})

;; role handlers

(defn roles
  [_]
  {:status 200,
   :body {:roles (db/get-all-roles)}})

(defn create-role
  [{:keys [parameters]}]
  (let [data (:body parameters)
        _ (db/create-role! data)]
    {:status 201,
     :body {:success
            (str "Role "
                 (:role data)
                 " was created succesfully")}}))
