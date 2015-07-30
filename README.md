SmartLab
=======

Research facilities employ complex systems to expedite the data exploration process. 
These machines, though exteremly useful and impressive, often require a steep learning curve. 
The goal of this applicaiton is to reduce the entry barrier for new Vislab users by providing
a more natural way of communicating with our systems.

## Getting started

Check out the TACC Vislab website to get an idea of the environment and advanced resources I hope to automate: https://www.tacc.utexas.edu/vislab/

## Dependencies

- [Jsch library](http://www.jcraft.com/jsch/)


## Running on Glass

You can use your IDE to compile and install the application (highly recommended) or use
[`adb`](https://developer.android.com/tools/help/adb.html)
on the command line:

    $ adb install -r app-debug.apk

To start the application, say "ok glass, SmartLab" from the Glass clock screen or use the touch menu. 
Browse the list of available commands and choose the action you'd like the lab to perform. 
