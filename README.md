# BPM Game

The [BPM Game](https://bpmgame.org/) is a game that revolves around business process redesign. This repository contains the codebase.

The code has two parts:
- the servlet that runs the web-site
- the simulator that evaluates the processes designed by the game participants
Both parts of the code are in this repository.

The two parts of code are linked by a MySQL database. The simulator has a command for initializing the database.

At this point the code structure and the way in which it can be deployed is sparsely documented. This will be improved.

## The Servlet

The servlet is developed for Tomcat. The entry into the servlet code is in the WebContent folder.

## The Simulator

The simulator is developed as a Java executable that must be run periodically (e.g. as a cron job) to evaluate the participant models that are in the database. The entry into the simulator code is in the src/nl/tue/simulator/Simulator.java class.