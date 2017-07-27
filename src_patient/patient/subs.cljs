(ns patient.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub reg-sub-raw]]))


(reg-sub
 :chats
 (fn [db _]
   (:chats db)))
