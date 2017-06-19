(ns mobile-patient.screen.meds
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]))


(defn MedsScreen [{:keys [navigation]}]
  [ui/view {:style {:flex 1}}
   [ui/text "Meds"]])
