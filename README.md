1) Update software on Cloud shell ```sudo apt-get update```
2) Set Project Id for Gcloud ```gcloud config set project <project-id>```

3) Create a new service account. Assign role (eg: Owner)
4) Create a new key file (JSON) for this SA and download.
5) In Cloud Shell, upload the json key file
6) Export the Env variable: ```export GOOGLE_APPLICATION_CREDENTIALS=~/<keyfile>.json```
7) Create a new project

```mvn archetype:generate -DarchetypeGroupId=org.apache.beam -DarchetypeArtifactId=beam-sdks-java-maven-archetypes-examples -DgroupId=com.faeez -DartifactId=StreamingDataflow -Dversion="0.1" -Dpackage=org.apache.beam.examples -DinteractiveMode=false```

8) In Cloud Editor, you see the new folder. Right click "Open as Workspace"
9) In the pom add under properties ```<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>```

10) Gcloud already installed on Cloud shell. Login ```gcloud auth login```

11) Make a bucket in project <id> using. ```gsutil mb -p <project-id> -l us-central1 gs://mysbtest12345```


12) Create a dataset. IMPT: Click on the project id in BigQuery first.

13) Run the job:
```mvn -Pdataflow-runner compile exec:java  -Dexec.mainClass=com.pluralsight.StreamingLaptops  -Dexec.args="--inputFile=gs://mysbtest12345/Source/laptops.csv --tableName=playground-s-11-1af003ca:results.categoryCount --project=playground-s-11-1af003ca --stagingLocation=gs://mysbtest12345/staging/ --tempLocation=gs://mysbtest12345/temp/ --runner=DataflowRunner --region=us-central1"```
