(ns mobile-patient.screen.chat
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]))


(defn ChatScreen [{:keys [navigation]}]
  [ui/view {:style {:flex 1}}
   [ui/text "Chatssssss"]])
