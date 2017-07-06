(ns mobile-patient.screen.chart
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]))

(def d3-scale (js/require "d3-scale"))
(def d3-shape (js/require "d3-shape"))


(def padding-size 20)
(def tick-width (* 1.5 padding-size))
(def animation-durationms 500)
(def dimension-window (.get ui/Dimensions "window"))
;; (def graph-width (- (.-width dimension-window) 40))
;; (def graph-height (- (.-height dimension-window) 100))
(def graph-width 300)
(def graph-height 400)

(def styles
  {:tick-label-x {:position :absolute
                  :bottom 0
                  :font-size 12
                  :text-align :center}
   :ticks-y-container {:position :absolute
                       :top  0
                       :left 0}
   :tick-label-y {:position :absolute
                  :left 0
                  :background-color :transparent}
   :tick-label-y-text {:font-size 12
                       :text-align :center}
   :ticks-y-dot {:position :absolute
                 :width 6
                 :height 6
                 :background-color :black
                 :border-radius 100}})

(def data #js[#js{:date (new js/Date 2000 1 1) :value 83}
              #js{:date (new js/Date 2000 1 2) :value 85}
              #js{:date (new js/Date 2000 1 3) :value 98}
              #js{:date (new js/Date 2000 1 4) :value 79}
              #js{:date (new js/Date 2000 1 5) :value 83}
              #js{:date (new js/Date 2000 1 6) :value 95}
              #js{:date (new js/Date 2000 1 7) :value 85}
              ])
;;;;;;;;;;;;;
(defn create-line-graph [{:keys [data x-accessor y-accessor width height]}]
  (let [first-datum (first data)
        last-datum (last data)
        scale-x (doto (d3-scale.scaleTime)
                  (.domain #js[(x-accessor first-datum) (x-accessor last-datum)])
                  (.range #js[tick-width width]))
        min-y (- (->> data (apply min-key y-accessor) (y-accessor)) 2)
        max-y (+ (->> data (apply max-key y-accessor) (y-accessor)) 2)
        scale-y (doto (d3-scale.scaleLinear)
                  (.domain #js[min-y max-y])
                  .nice
                  (.range #js[height 0]))
        line-shape (doto (d3-shape.line)
                     (.x #(-> % x-accessor scale-x))
                     (.y #(-> % y-accessor scale-y)))
        ]
    {:data data
     :scale {:x scale-x :y scale-y}
     :path (line-shape data)
     :ticks (map (fn [datum]
                   {:x (-> datum x-accessor scale-x)
                    :y (-> datum y-accessor scale-y)
                    :datum datum
                    })
                 data)
     }))


(defn LineGraph [{:keys [y-accessor width height] :as props}]
  (let [line-graph (create-line-graph props)
        tick-x-format (-> line-graph :scale :x (.tickFormat nil "%b %d"))
        ]
    ;;(print line-graph)
    (fn []
      [ui/view {:style {:background-color "white"}}

       [ui/surface {:width width :height height}
        [ui/group {:x 0 :y 0}
         [ui/shape {:d (:path line-graph)
                    :stroke "#ff4500"
                    :stroke-width 2}]]]

       [ui/view {:key :ticks-x}
        (map-indexed (fn [i tick]
                       [ui/text {:key i
                                 :style (merge (:tick-label-x styles)
                                               {:width tick-width
                                                :left (- (:x tick)
                                                         (/ tick-width 2))})}
                        (-> tick :datum .-date tick-x-format)])
                     (:ticks line-graph))]

       [ui/view {:key :ticks-y
                 :style (:ticks-y-container styles)}
        (map-indexed (fn [i tick]
                       [ui/view {:key i
                                 :style (merge (:tick-label-y styles)
                                               {:width tick-width
                                                :left (- (:x tick) tick-width)
                                                :top (- (:y tick) (/ tick-width 2))})}
                        [ui/text {:style (:tick-label-y-text styles)}
                         (str (y-accessor (:datum tick)))]])
                     (:ticks line-graph))]

       [ui/view {:key "tick-y-dot"
                 :style (:ticks-y-container styles)}
        (map-indexed (fn [i tick]
                       [ui/view {:key i
                                 :style (merge (:ticks-y-dot styles)
                                               {:left (- (:x tick) 3)
                                                :top  (- (:y tick) 3)})}])
                     (:ticks line-graph))]
       ])))

(defn ChartScreen [{:keys [navigation]}]
  [ui/view {:style {:background-color "#E9E9EF" :padding 10}}
   [LineGraph {:data data
              :x-accessor #(.-date %)
              :y-accessor #(.-value %)
              :width graph-width
               :height graph-height}]
   [ui/view {:background-color "white" :margin-top 10 :padding 10}
    [ui/text {:style {:font-size 20}}  "Last temperature observations, C°"]]])
