package org.di.api.impl

import org.di.api.SourceRepository
import org.di.graph.Graph


public class Main {
    public static void main(String... args) {
        SourceRepository repository = new CarfaxLibSourceRepository(localDir: new File("D:/hackathon"));
        Graph g = new Graph(repository)
        g.initRank()
        drawRank(g)
//        repository.init().each {
//            println it.name + " "+it.version
//        }

    }

    static drawRank(Graph graph) {
        String content = "digraph G { ranksep=3; nodesep=0.1; node [shape=point,width=.75,height=.5,fontsize=5];\n"
        def levels = graph.nodes.groupBy {it.rank}.keySet().sort{-it}
        content += "  {  node [shape=none]; edge [style=invis]; \n"
        content += "    "+levels.join(" -> ")
        content += "; \n"

        content += "  }\n"

        graph.nodes.groupBy {it.rank}.sort{-it.key}.each { rank, rankNodes ->
            content += "   { rank = same; ${rank};"
            content += rankNodes.collect {fix(it.projectSource.name)}.join ("; ")
            content += "   }\n"
        }

        graph.nodes.each { node ->
            node.outgoing.each { dependency ->
                String edgeStyle = ""
                if (dependency.dependency.version.toString() != dependency.to.projectSource.version.toString() ) {
                    edgeStyle =  "[color=red,style=\"setlinewidth(4)\"]"
                    println node.projectSource.name + " depends on "+dependency.to.name + " version "+dependency.dependency.version.toString()+ " ("+dependency.to.projectSource.version.toString()+")"
                }
                if (dependency.cyclic) {
                   edgeStyle = "[color=blue,style=\"setlinewidth(8)\"]"

                }
                content += "   " + fix(node.projectSource.name) + " -> " + fix(dependency.to.name) + " "+edgeStyle+"; \n"
            }
        }
        content += "}"
        File output = new File("d:/temp/gv.dot")
        output.delete()
        output << content
        def proc = """cmd /c C:\\cfx)\\graphviz\\bin\\dot.exe -Tpng D:\\temp\\gv.dot -o d:\\temp\\gv.png""".execute()
        proc.waitFor()
        "cmd /c d:\\temp\\gv.png".execute()
    }


    static String fix(String n) {
        n.replaceAll("-", "_")
    }
}
