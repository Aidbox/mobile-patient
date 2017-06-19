(ns mobile-patient.screen.vitals
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]))


(defn VitalsScreen [{:keys [navigation]}]
  [ui/view {:style {:flex 1}}
   [ui/text "Vitals"]])
