(ns mobile-patient.screen.meds
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]))


(defn MedsScreen [{:keys [navigation]}]
  [ui/table-view
   [ui/section
    [ui/cell {:title "Medication 1" :on-press #()}]
    [ui/cell {:title "Medication 2" :on-press #()}]
    [ui/cell {:title "Medication 3" :on-press #()}]
    ]])
