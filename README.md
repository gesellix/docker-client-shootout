# Compare Docker clients for the Java/JVM

The article [Docker for Java Developers: Docker over HTTP/REST](https://www.javacodegeeks.com/2017/10/docker-java-developers-docker-http-rest.html)
considers [Spotify's docker-client library](https://github.com/spotify/docker-client) as de-facto choice for JVM based applications.
Reading the broad overview of examples I wondered how they would look like when using other Java libraries for the Docker Engine API.
I personally considered another library ([github.com/docker-java](https://github.com/docker-java/docker-java)) to be
the de-facto choice, due to its popularity. Since I also maintain yet another library based on Groovy ([github.com/gesellix/docker-client](https://github.com/gesellix/docker-client)),
I couldn't resist but had to dig deeper and try to compare those three libraries.

This repo now contains three Maven projects, each using another Docker client library. The examples are taken
from the initially mentioned article and have been adopted to the respective lib:  

- [docker-java/docker-java](https://github.com/docker-java/docker-java)
- [gesellix/docker-client](https://github.com/gesellix/docker-client)
- [spotify/docker-client](https://github.com/spotify/docker-client)

## Scope

Though I consider Gradle to be superior, discussing the build tool is out of scope. I also refrained
from using Groovy. The focus should stay on the actual Docker client libraries, with their feature set,
performance, maintainability, compatibility, etc.

That said, there's not much value in simply comparing a list of features: some libraries might fill another niche, so
we cannot assume that this repository could define some kind of requirements for a Docker client. Please
consider this repository as a simple playground. Comparing the libraries is more or less a by-product of my curiosity.

## Bias everywhere

I'm aware that "my own lib" won't look so beautiful when being used in plain Java, but I guess I'm biased enough
to ignore that. There's another bias inherently given due to the given set of examples taken from the article:
I didn't extend the examples with other features, yet. That's why you're going to miss demos using the Swarm Mode,
Docker Stack, TLS configuration, etc. These should be added in the future, so please feel free to submit pull requests. 

## Contributing

I'm interested in adding more examples to show the maturity of each library. The examples should be kept up-to-date
with the Docker Engine API. When you'd like to see a specific aspect to be compared (maybe you found another library?),
please file an issue or go ahead and submit a pull request.
