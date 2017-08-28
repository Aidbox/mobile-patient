(ns mobile-patient.component.gesture-view
  (:require [reagent.core :as r]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            ))

(defn horiz-direction [e]
  (let [e1 (first (.. e -touchHistory -touchBank))
        dx (- (.-currentPageX e1) (.-startPageX e1))]
    (cond
      (> dx 30) :right
      (< dx -30) :left)))

(defn vert-direction [e]
  (let [e1 (first (.. e -touchHistory -touchBank))
        dx (- (.-currentPageY e1) (.-startPageY e1))]
    (cond
      (> dx 5) :up
      (< dx -5) :down)))

(defn log [e]
  (print (first (.. e -touchHistory -touchBank))))

(defn gesture-view [child]
  (let [state (r/atom 0)]
    (fn []
     [ui/view {:onMoveShouldSetResponder (fn [e] (not (boolean (vert-direction e))))
               :onResponderRelease (fn [e] (case (horiz-direction e)
                                             :right (swap! state inc)
                                             :left (swap! state dec)))

               :style {:border-width 1}}
      [ui/text @state]
      child
      ])))
