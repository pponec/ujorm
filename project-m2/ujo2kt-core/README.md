# Description of key-value architecture with POJO support inspired by Ujorm framework

## Introduction

This document describes a design for an `key-value` API that will work with plain POJOs. 
The solution assumes new meta-model classes generated according to the POJO template - for example by a Maven plugin.
An interface [Ujo](http://ujorm.org/javadoc/org/ujorm/Ujo.html) will be unnecessary and therefore the API cannot be
backward compatible with the current [Ujorm](https://ujorm.org/) framework.
However, removing the Ujo interface will complicate implementation
some services, such as managing parameters in a project
[DemoHotels](https://hotels.ujorm.org/source?src=org.ujorm.hotels.gui.hotel.HotelTable) .


## More information

For more information see the root files of the project.