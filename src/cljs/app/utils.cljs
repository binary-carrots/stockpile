(ns app.utils
  (:require [helix.dom :as d]))

(defn loading-status
  [status body]
  (case status
    200 body
    403 (d/p "You are not autharized to view this page")
    401 (d/p "Please sign in first")
    (d/p "Loading")))