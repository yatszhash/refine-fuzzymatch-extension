# refine-fuzzymatch-extension
Open Refine Extension to resolve ambiguity

## project purposes
OpenRefine is the cool and established tool to clean data.

However, to match or parse dirty "user-specific" data more easily or more automatically, 
I think it requires more features. <!-- learnabiligy-->

Of course, there are already some services to support these points (e.g dedupe, elastic search), 
I think it worth implementing it in OpenRefine because of its rich current features and its existing community.

<!--In addition, some algorithms in open refine like clustering assumes alpha-numerical texts,
so this project also aimed to support non-alphanumerical texts.-->

## current feature
### experimental
#### fuzzy matching of records between projects
##### 1. fuzzy match
###### What is this
The destination project is the project you want to add some information from another project.  
The source project is the project offers the destination project some additional information from its rows.

The match is done by comparing each key column (field) of the source project and each that of the destination project.  
**Currently, the match algorithm is based on wolfgarbe's [SimSpell](https://github.com/wolfgarbe/SymSpell).**  
**It finds max k similar rows in the range of edit distance d.**  
The current supported edit distance is only [the optimal string alignment distance](https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance#Optimal_string_alignment_distance).    
Some text normalization or standardization (e.g CNTK normalization, to lower case) is done in comparing,  

###### How to use
You can use this fuzzy match from "fuzzyCross" funciton in grel in the following way.
1. create indices from the column header menu of the key columns of the source project in the source project's view.
2. get row objects with GREL function "fuzzyCross"  in "edit" or "transform" of the destination project's view.  
   It's a similar function with "Cross" in OpenRefine, but a more complex one than it.  
   
   Here, currently, "similar" means only "have common characters with a similar aliment".  
   For example, 
   
        fuzzyCross(
                   row,    // dest row object
                   [ "DestinationKeyColumnName1",  "DestinationKeyColumnName2"],   //dest key column names
                     sourceProjectName,    
                    [ "SourceKeyColumnName1",  "SourceKeyColumnName2"],    //source key column names
                    [1, 3],   // max distances for each key
                    10,   // max number of reaturned rows
                    [15, 5]  //optional,   prefix lengths to compare
                    )
   
   Each element in each array of the arguments is for each key pair.   
   i.e. It compare "DestinationKeyColumnName1" column's value of the destination row 
   and that of "SourceKeyColumnName1" in the source rows with max edit distance 1 and prefix length 15. 
   
   
###### known limitation
1. can't flush indices based on the changes of the source project
   This is due to open refine's limitation.  
   If you change the cell value of the key field or rows for "source project",  
   you're required to create the indices again from column menu by yourself. 
    
2. slow to construct index with long sentences (> about 15) with long prefix and not short distance (> 5).

### production


<!--
## upcoming feature
1. reconciliation integration
1. learnable records match
2. fuzzy parsing 
3. user or project dictionary
4. co-operation with external service (e.g elastic search) 
-->

## Getting Started
copy this project directory into the extention directory of your open refine. (recommend remove .git folder)
For detail, please read the extention inroduction in open refine document.

### Prerequisites
- OpenRefine 2.8
- Java 8

### Installing

copy this project into the "extensions" folder of OpenRefine


## Running the tests


### Break down into end to end tests

### And coding style tests


## Deployment


## Built With

<!--- 
* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds
-->

## Contributing
<!--- 
Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.
-->

## Versioning
<!--- 
We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 
---> 
## Authors

**Vern1erCa11per** - *Initial work*

<!--
See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.
-->
## License

This project is licensed under Apache 2.0 - see the [LICENSE.md](LICENSE.md) file for details.
The License files for the dependencies are in [LICENSES](LICENSES).

## Acknowledgments

## Notice  
The current version is under development and the destructive changes may be added. 
