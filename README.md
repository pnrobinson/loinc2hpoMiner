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
java -jar target/loinc2hpo-miner-0.2.2.jar 
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

## Setup

Before using loinc2hpoMiner, we need to set a few things up using the ``configuration`` menu.

1. Set path to the LOINC ``LoincTableCore.csv`` file, which is freely available (after registration) at this site: https://loinc.org/downloads/loinc-table/
2. Download HPO file (activating this menu item will download the latest hp.json file to the loinc2hpominer user directory)
3. Set path to curation file.  It is easiest to clone this repository (https://github.com/TheJacksonLaboratory/loinc2hpoAnnotation).
Point the tool to the loinc2hpo-annotations.tsv file. If you would like to contribute new annotations, please checkout the develop branch and make a pull request against develop.
4. Set biocurator ID. This could be your ORCID id (e.g., ORCID:0000-0000-0000-0042) or something like HPO:probinson
5. The show settings item shows the results of the first four steps. The show annotations item is still work in progress.
