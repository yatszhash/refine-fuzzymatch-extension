# refine-fuzzymatch-extension

One Paragraph of project description goes here

## project purposes
OpenRefine is the very cool and established tool to clean data.

However, to match or parse dirty "user-specific" data more easily or more automatically, 
I think it requires more features with

**1. ambiguity
2. learnability**

Of course, there are already some services to support these points (e.g dedupe), 
I think it worth implementing it in OpenRefine because of its rich current features and its existing community.

In addition, some algorithms in open refine like clustering assumes alpha-numerical texts,
so this project also aimed to support non-alphanumerical texts.

## current feature
### experimental
1. fuzzy matching of records between projects
    - fuzzyCorss (grel Function)
        - known limitation
            1. can't reconstruct and write
            2. can't flush based on change of project
            3. very slow to construct
            4. large memory consumption

### production



## upcoming feature
1. reconciliation integration
1. learnable records match
2. fuzzy parsing 
3. user or project dictionary
4. co-operation with external service (e.g elastic search) 


## Getting Started

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

**Vern1erCa11per** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)

<!--
See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.
-->
## License

This project is licensed under Apache 2.0 - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

## Notice  
The current version is under development and the destructive changes may be introduced. 

- The fuzzy matching algorithm is inspired by symspell
