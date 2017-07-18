(ns mobile-patient.route-helpers
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]))

;; NAVIGATION BUTTONS

(defn add-chat-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Contacts")
                           :style {:margin-right 20}
                           :underlay-color color/grey}
   [ui/icon {:name "add-circle" :size 36 :color color/pink}]])

;; NAVIGATION ROUTES

(defn menu-button [navigation]
  [ui/touchable-highlight {:style {:margin-left 10}
                           :on-press #(navigation.navigate "DrawerOpen")
                           :underlay-color color/grey}
   [ui/icon {:name "menu" :size 24 :color :black}]])


(defn stack-navigator [routes]
  (r/reactify-component
   (ui/StackNavigator (clj->js routes)
                      (clj->js {:headerTitleStyle {:fontWeight :normal}}))))

(defn stack-navigator-back-button [props]
  (r/as-element
   [ui/touchable-highlight {:on-press #(props.navigation.goBack nil)
                            :style {:margin-left 15}
                            :underlay-color color/grey}
    [ui/icon {:name "chevron-left" :size 36 :color color/pink}]]))

(defn drawer-nav-opts
  ([title]
   (drawer-nav-opts title nil))
  ([title header-right]
   (fn [props]
     #js{:title title
         :headerLeft (r/as-element [menu-button props.navigation])
         :headerTitleStyle #js{:paddingLeft 10
                               :fontSize 19
                               :fontWeight "bold"
                               :color color/grey}
         :headerRight (when header-right (r/as-element [header-right props.navigation]))})))
