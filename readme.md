Crystals
======================= 

2D graphical representation of a tile puzzle solver, experimenting with different techniques in an attempt to solve a puzzle of varying complexity.
Work in progress and by no means a polished product and should be used only to illustrate how not to solve a tile tessellation puzzle.
This software is incomplete and although will never solve the 64x64 tile problem (not in our lifetime) it may be possible to solve smaller, less complex version of the same style of puzzle once complete.  Minor changes have been made since this was started to enable compilation under java 1.7.

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

Issues
-----------

This is a work in progress, whereby the work has clearly stopped for the time being.  There are a number of issue to be addressed

* No Solution Detection
* No Solution Notification
* A leak exists within the tile reuse loop resulting in a miss count of tiles over time 


Running the application
-----------

 
#### To compile:

*     ant

#### To run:
   
   * export CLASSPATH=$CLASSPATH:Crystals.jar
   * or
   * cd into folder containing GraphPanel.class

   * java -Xms1024m GraphPanel

    

#### To rebuild Eclipse Project:
*     mvn eclipse:clean eclipse:eclipse -DdownloadSources -Declipse:useProjectReferences=false -Dwtpversion=2.0

#### JUnit
* 	from within Eclipse right click the Crystals project and Run As->JUnit Test
	
	
#### Ant build of jar file
*     ant createjar
