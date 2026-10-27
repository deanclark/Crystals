Crystals
======================= 

2D graphical representation of a tile puzzle solver, experimenting with different techniques in an attempt to solve a puzzle of varying complexity.
Work in progress and by no means a polished product and should be used only to illustrate how not to solve a tile tessellation puzzle.
This software is incomplete and although will never solve the 64x64 tile problem (not in our lifetime) it may be possible to solve smaller, less complex version of the same style of puzzle once complete.

#### Tile Placement: 
!["Image of tiles place on the placement area"](https://github.com/deanclark/Crystals/blob/master/Doxygen/Screenshot-Crystals-tiles.png)


#### Random Infill Placement: 
!["Image of tiles following random infill using remaining tiles"](https://github.com/deanclark/Crystals/blob/master/Doxygen/Screenshot-Crystals-randomFill.png)

#### Random Infill Placement with gaps showing: 
!["Image of tiles with random infill 2  using remaining tiles"](https://github.com/deanclark/Crystals/blob/master/Doxygen/Screenshot-Crystals-randomFill2.png)

#### Floating Tiles Illustrating the animated elasticity : 
!["Image of floating tiles where the elastic tension between tile is being used to attract neighbouring tiles"](https://github.com/deanclark/Crystals/blob/master/Doxygen/Screenshot-Crystals-floating.png)


#### Floating Tiles with interconnecting wires: 
!["Image of interconnected floating tiles"](https://github.com/deanclark/Crystals/blob/master/Doxygen/Screenshot-Crystals-floatingWires.png)


Controls
-----------

* S	Start
* Q	Quit (Second Q will exit the application)
* C	Continue following a quit

Options
-----------

* **Floating** - on: tiles float and are pulled together by wires; off: the frame based search (Show / Infill) places tiles on the grid.
  In floating mode the tiles grow a crystal on the grid of a square playing area (about sqrt(n) x sqrt(n) cells, centred on the panel):
  a corner tile is locked into the top-left corner first; each step takes a random matching wire from a locked tile and places the tile at
  its other end in the neighbouring cell, turned so the matching edges touch. A tile is only placed where it fits the square: grey (outside)
  edges must face outside it and nowhere else, so corner tiles only fit corners and edge tiles only the border. Unlocked corner and edge
  tiles drift towards a corner or side of the square (shared out by tile id) with their grey edges facing outwards. Tiles never overlap.
  A tile is also only placed when every tile already beside that cell matches it edge to edge, so locked neighbours always match; where nothing
  fits, a hole is left for the moment. The grid is fixed when the corner tile is locked, and nothing grows until the panel has a size.
  Holes are repaired every iteration: a free tile that matches every neighbour is placed; otherwise the tiles that stand in the way (up to two
  neighbours, or a tile sitting in another cell that would fit) are unlocked and set free so a tile can go in, and the cells they vacated are
  refilled. The swap is kept only if the number of placed tiles does not go down, otherwise it is put back, so the count never falls.
  Holes are looked at from the corner outwards (border ring first, then the interior row by row), 30 attempts per iteration, so early holes
  are dealt with early. Crystal tiles keep their lock until a repair unlocks them, so "Locks in Use" equals the tiles placed (also shown as
  "Placed: n of 256"). About 215 of the 256 tiles are placed within 100 iterations and 220-230 after a few hundred, with every neighbouring
  pair matching; the rest are left waiting around the board.
* **Wireframe** - on: placed tiles are drawn as outlined coloured segments; off: segments are filled with their colour (both modes).
* **Large** - tile display size (15 or 28 px tiles, 6 or 40 px frame cells). Tiles butt up rather than overlap at either size. In a dense clump the overlap resolver keeps pushing for up to 60 passes and then moves any tile it could not separate to the nearest free spot.
* **Active** - floating mode only: let the elastic wires of unlocked tiles pull on their neighbours.
* **Random**, **Id's**, **Rotate**, **Wires** - random fill, show tile ids, animate rotation, show wires.

Issues
-----------

This is a work in progress, whereby the work has clearly stopped for the time being.  There are a number of issue to be addressed

* No Solution Detection
* No Solution Notification
* A leak exists within the tile reuse loop resulting in a miss count of tiles over time 


Running the application
-----------

 
#### Build and run

Use the Gradle wrapper from the repository root:

* Windows: `gradlew.bat clean build`
* macOS/Linux: `./gradlew clean build`
* Run the desktop application: `gradlew.bat run` (Windows) or `./gradlew run`
* Build the Android-consumable shared library: `gradlew.bat crystalsCommonJar` (Windows) or `./gradlew crystalsCommonJar`

The desktop application jar is written to `build/libs/`. `CrystalsCommon.jar`, containing only the `crystalscommon` package, is written to `build/android-libs/` and compiled for Java 11 bytecode (`options.release = 11`). `uTurnAnd/libs/CrystalsCommon.jar` must be rebuilt from here and copied across whenever the `crystalscommon` package changes. Gradle itself runs on JDK 17 or newer; this project was verified with JDK 25.

All 23 JUnit tests pass (`./gradlew test`). The old empty Eclipse stubs (`fail("Not yet implemented")` and tests with no assertions) were removed; the remaining tests cover tile drawing, collision, backtracking and the floating-mode crystal (`FloatingLayoutDiagnosticsTest`, which runs the real 256 tiles headless).
