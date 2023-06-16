Utility for exporting your decorations inventory to Honey Hunter, directly from your save-file

# Releases

If you want to just download the exectuable, check the releases page: https://github.com/clicksilver/mhw_decos_csv/releases/

# Requirements
* Java 8u162+
* Maven 

# Build
```
$ mvn package
```

# Usage 
```
$ java -jar exporter\target\exporter-1.3-jar-with-dependencies.jar PATH_TO_SAVE_FILE
```

# To do

* ~Decrypt the save file~
* ~Export to MHW DB~
* ~Create an executable and distribute~
* Make it read from equipped slots

# Credit

## MHW modding discord

* [legendff](https://github.com/LEGENDFF/mhw-Savecrypt) - save decrypting tool
* Deathcream - decoration byte offsets
* Vuze - script prototype
* Ice - jewel item ID to names
* [TanukiSharp](https://github.com/TanukiSharp/MHWSaveUtils)
* [AsteriskAmpersand](https://github.com/AsteriskAmpersand/MHW-Save-Editor)

## Others

* [TheNameKevinWasTaken](https://github.com/TheNameKevinWasTaken/mhw-deco-exporter) - honeyhunter deco export format
