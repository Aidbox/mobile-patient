(ns mobile-patient.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-greeting
  (fn [db _]
    (:greeting db)))

(reg-sub
 :get-in
 (fn [db [_ path]]
   (get-in db path)))
