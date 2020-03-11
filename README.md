Utility for exporting your decorations inventory to Honey Hunter, directly from your save-file

# Requirements
* Java 8u162+
* Maven 

# Build
```
$ mvn package
```

# Usage 
```
$ java -jar  target\mhw_decos_csv-1.0.jar DECRYPTED_SAVE_FILE
```

# To do

* Decrypt the save file
* Export to MHW DB

# Credit

## MHW modding discord

* [legendff](https://github.com/LEGENDFF/mhw-Savecrypt) - save decrypting tool
* Deathcream - decoration byte offsets
* Vuze - script prototype
* Ice - jewel item ID to names

## Others

* [TheNameKevinWasTaken](https://github.com/TheNameKevinWasTaken/mhw-deco-exporter) - honeyhunter deco export format
