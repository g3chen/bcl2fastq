##BCL2FastQ Decider

Version 1.0, SeqWare version 1.1.1-gsi

###Overview

This decider launches the [BCL2FastQ (AKA Casava) Workflow](../workflow-casava) to demultiplex and convert BCL files from an Illumina sequencer run to FASTQ format. This decider assumes paired-end reads, so if this is not the case, the read-ends parameter must be used.

The decider operates as follows:
- retrieves all lanes that are available from the sample and lane provenance providers (specified in the provenance settings file)
- retrieves all lanes that have associated analysis from the analysis provenance providers (specified in the provenance settings file)
- calculates the difference between the above two lane name sets to determine the set of lanes that have not be analyzed
- creates a SeqWare IUS-LimsKey (a SeqWare object that is used to link LIMS data from provenance providers such as Pinery and SeqWare to SeqWare workflow runs) for each lane and a SeqWare IUS-LimsKey for each sample in the associated lane
- schedules a separate SeqWare workflow run for each lane and links the workflow run to the appropriate SeqWare IUS-LimsKey(s)

###Compile

```
mvn clean install
```

###Testing

```
mvn clean verify \
-DskipITs=false \
-DworkingDirectory=/path/to/tmp/ \
-DschedulingHost=oozie-node \
-DwebserviceUrl=http://test-seqware-webservice:8080/seqware-webservice-1.1.1-gsi \
-DprovenanceSettingsPath=/path/to/test-provenance-settings.json \
-DdbHost=localhost \
-DdbPort=5432 \
-DdbUser=user-that-can-create-db
```

An example provenance settings file:
```
[ {
  "type" : "ca.on.oicr.gsi.provenance.SeqwareMetadataLimsMetadataProvenanceProvider",
  "provider" : "seqware",
  "providerSettings" : {
    "SW_METADATA_METHOD" : "webservice",
    "SW_REST_URL" : "http://test-seqware-webservice:8080/seqware-webservice-1.1.1-gsi",
    "SW_REST_USER" : "user",
    "SW_REST_PASS" : "password"
  }
}, {
  "type" : "ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider",
  "provider" : "seqware",
  "providerSettings" : {
    "SW_METADATA_METHOD" : "webservice",
    "SW_REST_URL" : "http://test-seqware-webservice:8080/seqware-webservice-1.1.1-gsi",
    "SW_REST_USER" : "user",
    "SW_REST_PASS" : "password"
  }
} ]

```

###Usage

java -jar Decider.jar --wf-accession \<bcl2fastq-workflow-accession\> --provenance-settings /path/to/provenance-settings.json

###Options

**Required**

Parameter | Type | Description \[default\]
----------|------|-------------------------
provenance-settings | String (Path) | Path to provenance settings json
wf-accession | Integer | Bcl2FastQ workflow accession

**Optional**

Parameters to filter/select the set of lanes to operate on:

Parameter | Type | Description \[default\]
----------|------|-------------------------
check-wf-accessions                      | String  | The comma-separated, no spaces, workflow accessions of the workflow that      
                                                     perform the same function (e.g. older versions). Any files that have been   
                                                     processed with these workflows will be skipped.
after-date                               | String  | Include only lanes created after the specified date
before-date                              | String  | Include only lanes created before the specified date
include-instrument                       | String  | Include only lanes with sequencer run attribute "instrument name"  
include-lane                             | String  | Include only lanes with lane name
include-sequencer-run                    | String  | Include only lanes with sequencer run name
include-sequencer-run-platform-model     | String  | Include only lanes with sequencer run platform model
include-study                            | String  | Include only lanes with study (determined by associated samples)
exclude-instrument                       | String  | Exclude lanes with sequencer run attribute "instrument name
exclude-lane                             | String  | Exclude lanes with lane name
exclude-sequencer-run                    | String  | Exclude lanes with sequencer run name
exclude-sequencer-run-platform-model     | String  | Exclude lanes with sequencer run platform model
exclude-study                            | String  | Exclude lanes with study (determined by associated samples)
all                                      | none    | Operate on all lanes defined in the provenance-settings json

Additional optional parameters include:

Parameter | Type | Description \[default\]
----------|------|-------------------------
help                                     | none    | Display help
verbose                                  | Boolean | Log verbose output
host                                     | String  | Used only in combination with --schedule to schedule onto a specific host. If 
                                                     not provided, the default is the local host [local hostname]
dry-run or test                          | Boolean | Dry-run/test mode. Prints the INI files to standard out and does not submit the workflow [false]
no-meta-db or no-metadata                | Boolean | Prevents metadata writeback (which is done by default) by the Decider and that is subsequently passed 
                                                     to the called workflow which can use it to determine if they should write metadata at runtime on the cluster [false]
disable-run-complete-check               | Boolean | Disable checking that the file "oicr_run_complete" is present in the "run_dir" [false]
no-null-created-date                     | Boolean | Set the filter comparison date to "last modified" date if "created date" is null [false]
force-run-all or ignore-previous-runs    | Boolean | (WARNING: use with caution) Forces the decider to run all matches regardless of whether they've been run before or not [false]
ignore-previous-lims-keys                | Boolean | (WARNING: use with caution) Ignore all existing analysis (workflow runs and IUS skip) [false]
launch-max                               | Integer | The maximum number of workflow runs to launch at once [10]
study-to-output-path-csv                 | String (path) | The absolulte path to the "Study To Output Path" CSV file that defines the workflow "output-prefix"
output-path                              | String (path) | Absolute path of directory to put the final files
output-folder                            | String (path) | Path to put the final files, relative to output-path


**Note**
All of the [workflow properties](../workflow-casava) can be overridden by providing <property, value> pairs in the command, for example:
```
java -jar /path/to/decider.jar --wf-accession 000000 --provenance-settings /path/to/provenance-settings.json -- --property1 value1 --property2 value2
```

##Support

For support, please file an issue on the [Github project](https://github.com/oicr-gsi) or send an email to gsi@oicr.on.ca .
