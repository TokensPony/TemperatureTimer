package com.bluetooth.mwoolley.microbitbledemo;

public class TemperatureConfig {
    public String configName;
    public int lowTemp;
    public int highTemp;
    public int delayMinutes;

    public TemperatureConfig(String name, int lTemp, int hTemp, int delay){
        configName = name;
        lowTemp = lTemp;
        highTemp = hTemp;
        delayMinutes = delay;
    }

}
