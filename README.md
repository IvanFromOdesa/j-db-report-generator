# j-db-report-generator
Java sql db to xml parser.

Connects to the db and loads table contents into xml. The configuration can be found at ```.properties```.

## Warning. This will load every row of every table, if you do not specify limits. If the dataset is too big a stackoverlow error might arise.

Pass the ```.properties``` path as the command line argument when running the application.

To configure logging, pass ```-Djava.util.logging.config.file``` specifying the location of custom ```logging.properties``` file.
