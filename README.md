# MoonRoverSimulator
Little Scala project to simulate the communication between moon rovers and command center

### Run it

```
sbt "run-main simulator.DemoSimulator"
```

### How it works

Upon starting, the simulator first generates a data file to mimic the location data of our rovers at each second, one record per line, with the format as:

**rover_id direction speed position.x position.y turn_angle**

Then the command center will read the file and dispatch the data to each rover to simulate the communication.

The project configuration file is at */resources/application.conf* , from where you can tweak several parameters to run, such as *data_file_path*, *simulation_duration_in_minutes*, *rover.count* and so on.

### Test it

```
sbt test
```

By default the unit tests include a 1 minute simulation case, you can disable it by setting *simulation_duration_in_minutes* to 0 under *test/resources/application.conf*.
