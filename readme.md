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

The existing JUnit tests include unfinished Eclipse stubs (`fail("Not yet implemented")`) that fail by design, so `test` and `build` report failures until those tests are completed (45 of 82 at the time of writing; the rest pass, including the tile drawing, collision and backtracking tests).
