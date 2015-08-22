package org.di.graph.visualization

import org.di.graph.Graph
import org.di.graph.Node

class GraphVizGenerator {
    Graph graph
    File script
    File graphic
    //see: http://www.graphviz.org/doc/info/output.html
    //     http://www.graphviz.org/doc/info/attrs.html
    String outputType = "png"
    String dotCommandLocation
    String commandExecutionPrefix
    String commandOpenPrefix
    String shape = "point"

    GraphVizGenerator() {
        script = new File(System.getProperty("java.io.tmpdir")+"/di.dot")
        script.delete()
        graphic = new File(System.getProperty("java.io.tmpdir")+"/di.${outputType}")
        graphic.delete()
        if (System.getProperty("os.name").startsWith("Windows")) {
           dotCommandLocation = "C:\\cfx\\graphviz\\bin\\dot.exe"
            commandExecutionPrefix = "cmd /c "
            commandOpenPrefix = ""
        } else {
            dotCommandLocation = "/usr/local/bin/dot"
            commandExecutionPrefix = ""
            commandOpenPrefix = "open "
        }
    }

    def generate() {
        graph.initRank()
        String content = "digraph G { ranksep=1; nodesep=0.1; node [shape=${shape},width=.95,height=.95];\n"
        def levels = graph.nodes.groupBy {it.rank}.keySet().sort{-it}
        content += "  {  node [shape=none]; edge [style=invis]; \n"
        content += "    "+levels.join(" -> ")
        content += "; \n"

        content += "  }\n"

        graph.nodes.groupBy {it.rank}.sort{-it.key}.each { rank, rankNodes ->
            content += "   { rank = same; ${rank};"
            content += rankNodes.collect {drawNode(it)}.join (" ; ")
            content += "   }\n"
        }

        graph.nodes.each { node ->
            node.outgoing.each { dependency ->
                String edgeStyle = ""
                if (dependency.isStale() ) {
                    edgeStyle =  "[color=red,style=\"setlinewidth(4)\"]"
                    println node.projectSource.name + " depends on "+dependency.to.name + " version "+dependency.dependency.version.toString()+ " ("+dependency.to.projectSource.version.toString()+")"
                }
                if (dependency.cyclic) {
                    edgeStyle = "[color=blue,style=\"setlinewidth(8)\"]"

                }
                if (dependency.updateFailed) {
                    edgeStyle =  "[color=purple,style=\"setlinewidth(4)\"]"
                }
                content += "   " + fix(node.projectSource.name) + " -> " + fix(dependency.to.name) + " "+edgeStyle+"; \n"
            }
        }
        content += "}"
        script.delete()
        script << content
        String command = """${commandExecutionPrefix}${dotCommandLocation} -Tpng ${script.absolutePath} -o ${graphic.absolutePath}"""
        println " using the following graphviz command: $command"
        def proc = command.execute()
        proc.waitFor()
        println proc.in.text
        println proc.err.text

    }

    def reveal() {
        "${commandExecutionPrefix}${commandOpenPrefix}${graphic.absolutePath}".execute()
    }

    static String fix(String n) {
        n.replaceAll("-", "_")
    }

    static String drawNode(Node node) {
        fix(node.projectSource.name) + (node.buildFailed ? " [color=red] " : "" )

    }
}
