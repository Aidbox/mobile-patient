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
   [ui/icon {:name "menu" :size 36 :color color/grey}]])

(defn drawer-item
  ([title on-press]
   (drawer-item title on-press nil))
  ([title on-press style-props]
   [ui/touchable-highlight {:style (merge {:height 45
                                           :justify-content :center}
                                          style-props)
                            :on-press on-press
                            :underlay-color color/grey}
    [ui/text {:style {:margin-left 50
                      :font-size 24}} title]]))

(defn drawer-content [props]
  (let [username @(rf/subscribe [:get-in [:user :name 0 :text]])
        excluded #{"About App"}]
    [ui/view {:style {:background-color "#f4f4f4"
                      :flex 1}}
     [ui/view {:style {:background-color "white"
                       :height 100
                       :flex-direction :row
                       :justify-content :center}}
      [ui/view {:style {:width 48
                        :height 48
                        :border-radius 24
                        :background-color "#9e9e9e"
                        :align-self :center}}]
      [ui/text {:style {:font-size 20
                        :margin-left 10
                        :color "black"
                        :align-self :center}} username]]
     [ui/view {:style {:flex 1
                       :margin-top 30}}
      (for [route (-> props .-items) :when (not (excluded (.-key route)))]
        ^{:key (.-key route)}
        [drawer-item (.-routeName route) #(props.navigation.navigate (.-key route))])
      [drawer-item "Logout" #(do
                               (rf/dispatch [:initialize-db])
                               (rf/dispatch [:set-current-screen :login]))]]
     [drawer-item "About app" #(props.navigation.navigate "About App") {:margin-bottom 60}]]))

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
         :headerTitleStyle #js{:fontWeight "normal"
                               :color "#6e6e6e"}
         :headerRight (when header-right (r/as-element [header-right props.navigation]))})))
