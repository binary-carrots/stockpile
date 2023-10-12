(ns app.pages.users-page
  (:require [helix.core :refer [defnc $ <>]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            [promesa.core :as p]
            [lambdaisland.fetch :as fetch]))

(defnc user-list-item [{:keys [user]}]
  (d/li
   (d/div
    (d/p (d/strong "ID: ") (:id user))
    (d/p (d/strong "Name: ") (:username user))
    (d/p (d/strong "Email: ") (:email user)))))

(defnc users-list []
  (let [[users set-users] (hooks/use-state {})
        _ (hooks/use-effect
           :once
           (p/let [_res (fetch/get "http://localhost:3000/api/admin/users" {:accept :json
                                                                            :content-type :json
                                                                            :credentials :include})
                   body (:body _res)
                   _users (js->clj body :keywordize-keys true)]
             (set-users _users)))]
    (<>
     (if (:users users)
       (d/ul
        (map-indexed
         (fn [i user]
           ($ user-list-item {:key i :user user}))
         (:users users)))
       (d/p "error")))))

(defnc users-page []
  ($ users-list))