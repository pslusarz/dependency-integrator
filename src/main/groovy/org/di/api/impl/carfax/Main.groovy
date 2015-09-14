package org.di.api.impl.carfax

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.di.api.ProjectSource
import org.di.api.SourceRepository
import org.di.engine.BuildRecord
import org.di.engine.BuildRunner
import org.di.engine.BulkDependencyIncrementer
import org.di.engine.PastProjectSources
import org.di.engine.SpanningTreeBuilder
import org.di.engine.StalenessCalculator
import org.di.graph.Edge
import org.di.graph.Graph
import org.di.graph.Node
import org.di.graph.visualization.GraphVizGenerator


public class Main {
    public static void main(String... args) {
        SourceRepository repository = new CarfaxLibSourceRepository(localDir: new File("work/project-sources/"));
        staleness(repository)
        //playWithPastProjectVersions(repository)
        //displayVersions(repository)
        //drawGraphWithFailed(repository)
        //repository.downloadAll()
        // updateOneProject(repository, "dealerautoreports-commons")
        // demo(repository)
    }

    static staleness(CarfaxLibSourceRepository repository) {
        def projects = repository.init()
        Graph g = new Graph(projects)
        StalenessCalculator calc = new StalenessCalculator(g)
        println "STALENESS: "+calc.metric
        println "----- relative contribution per node:"
        calc.nodeImpact.sort {- it.value}.each { node, impact ->
           println node.name + " "+impact
        }
        println "----- most stale build files and their total cost:"
        calc.outdatedProjectImpact.sort {- it.value}.each { node, impact ->
            println node.name + " "+impact
        }
    }

    static updateOne(String projectName, Collection<ProjectSource> projects) {
        BulkDependencyIncrementer b = new BulkDependencyIncrementer(projectSource: projects.find {
            it.name == projectName
        }, projectSources: projects)
        b.increment()
    }

    static playWithPastProjectVersions(CarfaxLibSourceRepository repository) {
        def versionCounts = new PastProjectSources(localDir: new File('src/test/resources')).referencedVersionCounts()
        def projects = repository.init()
        projects.each { project ->
           project.versions.each {version ->
               if (versionCounts[project.name][version.toString()] == null) {
                   versionCounts[project.name][version.toString()] = 0
               }
           }
        }
        int referenced = 0
        int unreferenced = 0
        versionCounts.each {project, versions ->
println project
            println "   "+versions
            if (versions.find {k, v -> v > 0}) {
                int haveRefs = versions.findAll {k, v -> v > 0}.size()
               referenced +=  haveRefs
               unreferenced += versions.size() - haveRefs
            }
        }

        println "referenced: "+referenced
        println "unreferenced: "+unreferenced
    }

    static displayVersions(SourceRepository repository) {
        List<ProjectSource> projectSources = repository.init()
        projectSources.sort {-it.versions.size()}.each {
            println it.name + "  "+it.versions.size()
        }
    }

//    static drawGraphWithFailed(SourceRepository repository) {
//        def failedNames = ['carfax-auction-bridge', 'carfax-commons-controlm', 'carfax-core-consumer', 'carfax-cvs-navigator', 'carfax-logging-commons',
//                           'carfax-product-commons', 'carfax-xinfo', 'carfaxonline-java-acceptance', 'coffeescript-extensions',
//                           'consumer-fitnesse', 'consumer-testing-internal', 'cvs-repository-plugin', 'datasource-provider-domain',
//                           'dealerautoreports-commons-acceptance', 'git-repository-plugin', 'jsspec-runner',
//                           'messaging-adapters', 'name-in-lights-acceptance', 'quickvin-domain', 'rest-client-extensions',
//                           'subscriber-domain', 'VzMetadata', 'webdriver-fitnesse-extensions']
//        Graph graph = new Graph(repository)
//        graph.initRank()
//        failedNames.each { String failed ->
//
//            Node n = graph.nodes.find { it.name == failed }
//            if (n) {
//                n.buildFailed = true
//            } else {
//                println "could not find node for: " + failed
//            }
//        }
//        def gv = new GraphVizGenerator(graph: graph)
//        gv.generate()
//        gv.reveal()
//
//    }


    static demo(SourceRepository repository) {
        String projectName = "dealer-inventory-domain"
        //repository.downloadAll()
        Graph g = new Graph(repository)
        Graph dependents = new Graph(new SpanningTreeBuilder(world: g, treeRoot: projectName).connectedProjects)
        dependents.initRank()
        def gv = new GraphVizGenerator(graph: dependents)
//        gv.graph.nodes.each {
//            println it.name + " " + it.rank
//        }
//        BuildRunner br = new BuildRunner(projectSources: dependents.nodes.collect {it.projectSource} )
//        br.start(4)
//        def results = br.completeBuildRecords
//        results.findAll { it.result == BuildRecord.BuildResult.Failed }.each { currentBuild ->
//            dependents.nodes.find {it.name == currentBuild.projectSource.name }.buildFailed = true
//        }
        //   dependents.nodes.find {it.name == 'dealerautoreports-commons-acceptance'}.buildFailed = true

        gv.generate()
        gv.reveal()
        println "number of jars dependent on ${projectName}: " + (dependents.nodes.size() - 1)

//        def rank= 2
//        Map<Node, BulkDependencyIncrementer> incrementers = new HashMap<>().withDefault {node -> new BulkDependencyIncrementer(node: node)}
//        Collection<Node> levelProjects = dependents.nodes.findAll {it.rank == rank && !it.buildFailed && incrementers[it].increment()}
//        println "now will try to integrate rank ${rank}: "+ levelProjects
//
//        //levelProjects. {incrementers[it].increment()}
//        BuildRunner br2 = new BuildRunner(projectSources: levelProjects.collect {it.projectSource} )
//        br2.start(4)
//        def results2 = br2.completeBuildRecords
//        results2.findAll { it.result == BuildRecord.BuildResult.Failed }.each { currentBuild ->
//            Node failed = levelProjects.find {it.name == currentBuild.projectSource.name }
//            failed.outgoing.each {it.updateFailed = true}
//            failed.buildFailed = true
//            incrementers[failed].rollback()
//        }
//
//
//        gv.generate()
//        gv.reveal()

    }

    static updateOneProject(SourceRepository repository, String projectName) {
        repository.downloadAll()
        Graph g = new Graph(repository)
        Graph dependents = new Graph(new SpanningTreeBuilder(world: g, treeRoot: projectName).connectedProjects)
        dependents.initRank()
        def gv = new GraphVizGenerator(graph: dependents)
        gv.graph.nodes.each {
            println it.name + " " + it.rank
        }
//        BuildRunner br = new BuildRunner(projectSources: dependents.nodes.collect {it.projectSource} )
//        br.start(4)
//        def results = br.completeBuildRecords
//        results.findAll { it.result == BuildRecord.BuildResult.Failed }.each { currentBuild ->
//            dependents.nodes.find {it.name == currentBuild.projectSource.name }.buildFailed = true
//        }
        dependents.nodes.find { it.name == 'dealerautoreports-commons-acceptance' }.buildFailed = true

        gv.generate()
        gv.reveal()

        def rank = 2
        Map<Node, BulkDependencyIncrementer> incrementers = new HashMap<>().withDefault { node -> new BulkDependencyIncrementer(node: node) }
        Collection<Node> levelProjects = dependents.nodes.findAll {
            it.rank == rank && !it.buildFailed && incrementers[it].increment()
        }
        println "now will try to integrate rank ${rank}: " + levelProjects

        //levelProjects. {incrementers[it].increment()}
        BuildRunner br2 = new BuildRunner(projectSources: levelProjects.collect { it.projectSource })
        br2.start(4)
        def results2 = br2.completeBuildRecords
        results2.findAll { it.result == BuildRecord.BuildResult.Failed }.each { currentBuild ->
            Node failed = levelProjects.find { it.name == currentBuild.projectSource.name }
            failed.outgoing.each { it.updateFailed = true }
            failed.buildFailed = true
            incrementers[failed].rollback()
        }


        gv.generate()
        gv.reveal()

    }


    static buildAll(projects) {
        long start = System.currentTimeMillis()
        BuildRunner br = new BuildRunner(projectSources: projects)
        br.start(8)
        def results = br.completeBuildRecords
        long stop = System.currentTimeMillis()
        results.findAll { it.result == BuildRecord.BuildResult.Failed }.each {
            println it.projectSource.name + " " + it.result
        }

        println "Total time (ms): " + (stop - start)
    }

    static drawGraph(repository) {
        Graph g = new Graph(repository)
        g.initRank()
        g.cycles.each {
            println it
        }
        def gv = new GraphVizGenerator(graph: g)
        gv.generate()
        gv.reveal()
    }


}
