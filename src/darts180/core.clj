(ns darts180.core
  (:gen-class)
  (:use [seesaw [core :as s]
                [graphics :as g]
                [font :as f]]))

(def board-center (atom [0 0 ]))
(def board-radius (atom 0))

(def segment-numbers [20 1 18 4 13 6 10 15 2 17 3 19 7 16 8 11 14 9 12 5])

;; Calculating the relations of the sizes of the dart singles-doubles-triples segments
(defn- get-diameter [diameter percent]
  (-> diameter (/ 100) (* percent)))

(defn- get-outer-singles-diameter [diameter]
  (get-diameter diameter 95))

(defn- get-triples-diameter [diameter]
  (get-diameter diameter 60))

(defn- get-inner-singles-diameter [diameter]
  (get-diameter diameter 55))

(defn- get-semi-diameter [diameter]
  (get-diameter diameter 4))

(defn- get-bull-diameter [diameter]
  (get-diameter diameter 2))


(defn radian->degree "Converts radian to degree" [radian]
  (-> radian (/ Math/PI) (* 180)))

(defn fill-arc "Fills a pizza segment" [graphics position radius x color]
  (doto graphics
    (.setColor color)
    (.fillArc position position radius radius (-> x (* 18) (- 10)) 18)))

(defn draw-arc "Draws a pizza segment" [graphics position radius x color]
  (doto graphics
    (.setColor color)
    (.drawArc position position radius radius (-> x (* 18) (- 10)) 18)))

(defn draw-board "Draws the dart board" [widget graphics]
  (let [w (.getWidth widget) w2 (/ w 2)
        h (.getHeight widget) h2 (/ h 2)
        min-w2-h2 (min w2 h2)
        doubles-diameter (min w h)
        _ (reset! board-radius (/ doubles-diameter 2))
        _ (reset! board-center [(/ doubles-diameter 2) (/ doubles-diameter 2)])
        outer-singles-diameter (get-outer-singles-diameter doubles-diameter)
        outer-singles-center (-> doubles-diameter (- outer-singles-diameter) (/ 2))
        triples-diameter (get-triples-diameter doubles-diameter)
        triples-center (-> doubles-diameter (- triples-diameter) (/ 2))
        inner-singles-diameter (get-inner-singles-diameter doubles-diameter)
        inner-singles-center (-> doubles-diameter (- inner-singles-diameter) (/ 2))
        semi-diameter (get-semi-diameter doubles-diameter)
        bull-diameter (get-bull-diameter doubles-diameter)
        black (java.awt.Color/BLACK)
        bright (java.awt.Color. 247 236 170)
        red (java.awt.Color/RED)
        green (java.awt.Color/GREEN)
        semi-style (g/style :background green :foreground black)
        bull-style (g/style :background red :foreground black)
        border-circle-style (g/style :foreground black)
        draw-border-circle (fn [diameter] (g/draw graphics (g/circle min-w2-h2 min-w2-h2 (/ diameter 2)) border-circle-style))]

    (doseq [x (range 1 21) :let [singles-color (if (even? x) black bright)
                                 doubles-color (if (even? x) red green)]]
      (doto graphics
        (fill-arc 0 doubles-diameter x doubles-color)
        (fill-arc outer-singles-center outer-singles-diameter x singles-color)
        (fill-arc triples-center triples-diameter x doubles-color)
        (fill-arc inner-singles-center inner-singles-diameter x singles-color)))
    (draw-border-circle doubles-diameter)
    (draw-border-circle triples-diameter)
    (draw-border-circle outer-singles-diameter)
    (draw-border-circle inner-singles-diameter)
    (g/draw graphics (g/circle min-w2-h2 min-w2-h2 semi-diameter) semi-style)
    (g/draw graphics (g/circle min-w2-h2 min-w2-h2 bull-diameter) bull-style)))


(defn score-literal "Get a literal like T20 or D10 representing the score which corresponds to
                    the given point" [point]
  (let [x (.getX point) y (.getY point)
        [center-x center-y] @board-center
        distance (.distance point center-x center-y)
        distance-x (- x center-x) distance-y (- y center-y)
        angle (-> (Math/atan2 distance-x distance-y)  (radian->degree) (+ 180) (* -1) (+ 368) (mod 360))
        index (-> angle (/ 18) (Math/floor))
        number (nth segment-numbers index)
        radius @board-radius
        triples-diameter (get-triples-diameter radius)]
    (cond
      (<= distance (get-bull-diameter (* radius 2))) "D25"
      (<= distance (get-semi-diameter (* radius 2))) "S25"
      (<= distance (get-inner-singles-diameter radius)) (str "S" number)
      (<= distance triples-diameter) (str "T" number)
      (<= distance (get-outer-singles-diameter radius)) (str "S" number)
      (<= distance radius) (str "D" number)
      :else "Not in board")))

(defn redraw-board "Redraws the board canvas" []
  (config! board-canvas :paint draw-board))

(defn log-score "Log the on-mouse-over score of the board in the label" [board a-label]
  (listen board :mouse-moved
          (fn [e] (let [point (.getPoint e)
                        score (score-literal point)]
                    (text! a-label score)))))

(def board-canvas (s/canvas :background "#BBBBDD" :id :board :paint draw-board))
(def score-label (label :text "SCORE" :font (f/font "ARIAL-BOLD-20")))
(def content-panel (s/border-panel :hgap 10 :vgap 10 :center board-canvas :south score-label ))
(def my-frame (s/frame :width 500 :height 500 :title "Darts180" :content content-panel))

(defn log "Log" []
  (def remove-log (log-score board-canvas score-label)))

(defn start-app "Starts the Swing application" []
  (s/native!)
  (s/invoke-later (-> my-frame pack! show!))
  (log))

(defn -main
  "Run the application"
  [& args]
  (start-app))
