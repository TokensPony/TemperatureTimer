# Temperature Timer Milestone 2

## Summary  
In this second milestone I focused on the most important two most core elements of the project, those being the ability to connect to the BBC Micro Bit via Bluetooth in the app and reading a basic Temperature. Along with that I tried to implement various UI elements in preparation for features that are soon to come in the app such as the timer functionality, high/low temperature alarms, and database connections.

The base of this app utilizes some open source code from Martin Wooley's Micro Bit Blue application seen here:

https://github.com/microbit-foundation/microbit-blue

This code provides the BluetoothLE libraries and a basic interface for connecting to BBC Micro Bits and pulling data from it's various sensors. With this base I am creating my own interface for the temperature sensor that will allow for greater control and customization than what is provided in the stock pages.

The app I have so far will allow for connecting to BBC Micro Bit Devices, viewing current temperature date and converting that reading to
Farenheit or Celcius on the fly. I also have added the basic UI elements for choosing how long to have the timer run for once the temperature goes out of range.
