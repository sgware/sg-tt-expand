# Tandem Tales Logical Story World State Graph Expander

This is a [Story Graph Tool](https://github.com/sgware/story-graph-tools) that
expands all possible states of a
[Tandem Tales](https://github.com/sgware/tt-server)
[Logical Story World](https://sgware.github.io/tt-server/edu/uky/cs/nil/tt/world/LogicalWorld.html)
and writes them to a [Story Graph](https://github.com/sgware/story-graph) file.

States are expanded by complete breadth-first search, which can be limited to a
certain depth. Depth 4 means the graph will generate all states which can be
reached by taking 4 or fewer actions from the initial state. Only states from
which it is possible to reach a terminal state will be included in the story
graph, meaning it will always be possible to reach one of the story's endings.

## Download and Documentation

This tool is written in pure Java and depend on
[Story Graph Tools](https://github.com/sgware/story-graph-tools) and
[Tandem Tales](https://github.com/sgware/tt-server).

You can [download the pre-compiled JAR file here](build/jar).

The [JavaDoc API is here](https://sgware.github.io/sg-tt-expand).

You can download and compile these tools from source using
[Maven](http://maven.apache.org/) like this:
```
git clone https://github.com/sgware/story-graph-tools.git
cd story-graph-tools
mvn clean install
cd ..
git clone https://github.com/sgware/tt-server.git
cd tt-server
mvn clean install
cd ..
git clone https://github.com/sgware/sg-tt-expand.git
cd sg-tt-expand
mvn clean install
```

You can add this tool to a Maven project's `pom.xml` file like this:
```
<project>
  ...
  <dependencies>
    <!-- Tandem Tales Logical Story World State Graph Expander -->
    <dependency>
      <groupId>edu.uky.cs.nil</groupId>
      <artifactId>sg-tt-expand</artifactId>
      <version>1.0.0</version> <!-- use most recent version -->
    </dependency>
  </dependencies>
  ...
</project>
```

## Example Usage

For this example, you will need [Git](https://git-scm.com/) and
[Java](https://www.oracle.com/java/technologies/downloads/) installed and on
your path. You will also need a
[Tandem Tales logical story world](https://github.com/sgware/tt-server/tree/main/worlds).
This examples assumes you will download `tutorial.json` into the `build/jar`
folder of this project. You may want want to download the
[`sg-explore` tool](https://github.com/sgware/story-graph-tools/tree/main/build/jar)
so you can examine the graph that is generated.

```
# Clone this project.
git clone https://github.com/sgware/sg-tt-expand
cd sg-tt-expand/build/jar

# Download a story world and the story graph exploration tool.
curl -L -o tutorial.json https://raw.githubusercontent.com/sgware/tt-server/main/worlds/tutorial.json
curl -L -o sg-explore.jar https://raw.githubusercontent.com/sgware/story-graph-tools/main/build/jar/sg-explore.jar

# Show documentation for this tool.
java -jar sg-tt-expand.jar -help

# Generate the complete story graph for the 'tutorial' story world.
java -jar sg-tt-expand.jar tutorial.json

# Explor the story graph.
java -jar sg-explore.jar tutorial.zip

# Expand the graph only to depth 4.
java -jar sg-tt-expand.jar tutorial.json -depth 4 -out tutorial4.zip

# Explor the story graph.
java -jar sg-explore.jar tutorial4.zip
```

## Ownership and License

The Story Graph Library and this tool was originally developed by Stephen G.
Ware PhD, Associate Professor of Computer Science at the University of Kentucky
in 2026. Development of this software was sponsored in part by a grant from the
US National Science Foundation, #2145153.

This project is released under the
[General Public License version 3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).
In short, this means you are free to download, use, modify, and redistribute
this software as long as you continue to acknowledge the original copyright
holders and as long as you make the software that you create with these tools
freely and publicly available under a similar license.

See the license file for full details. The University of Kentucky retains all
rights not specifically granted.

This license allows you to use this software in commercial projects, but only if
you also release your project under a compatible open source license. If you
want to use this software in a project that is not open source, exceptions can
be granted by the copyright holders. Contact the University of Kentucky Office
of Technology Commercialization at `otcinfo@uky.edu` to discuss licensing this
software for other kinds of projects.

## Version History

- Version 1.0.0: First public release.

## Citation

Please cite this library like this:

> Stephen G. Ware, "Tandem Tales Logical Story World State Graph Expander,"
> GitHub, 2026. https://github.com/sgware/sg-tt-expand

BiBTeX entry:

```
@misc{ware2026storygraph,
  author={Ware, Stephen G.},
  title={Tandem Tales Logical Story World State Graph Expander},
  publisher={GitHub},
  year={2026},
  howpublished = {\url{https://github.com/sgware/sg-tt-expand}}
}
```