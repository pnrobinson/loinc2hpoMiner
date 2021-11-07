# loinc2hpoMiner
An application for curation of LOINC2HPO annotations.

## Building from source

Loinc2HpoMiner requires Java 16 or higher. 

Loinc2HpoMiner uses  [loinc2hpo](https://github.com/monarch-initiative/loinc2hpo). Currently,
this needs to be downloaded and installed locally.
```bazaar
git clone https://github.com/monarch-initiative/loinc2hpo
cd loinc2hpo
mvn install
```
Following this, download this repository and build it with maven.
```bazaar
git clone https://github.com/pnrobinson/loinc2hpoMiner
cd loinc2hpoMiner
mvn package
```

The app can then be started with
```bazaar
java -jar target/loinc2hpo-miner-0.1.1.jar 
```


## Building with IntelliJ
The Loinc2HpoMiner code uses the following to access data in the POM file.
```bazaar
 @Autowired
    BuildProperties buildProperties;
```
In order for the build system of IntelliJ to understand this, 
we need to set the following:
Settings/Build, Execution, Deployment/Build Tools/Maven/Runner: select the option "Delegate IDE build/run actions to Maven."
