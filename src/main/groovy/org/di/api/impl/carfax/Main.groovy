package org.di.api.impl.carfax

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Log
import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.SourceRepository
import org.di.engine.BuildRecord
import org.di.engine.BuildRunner
import org.di.engine.BulkDependencyIncrementer
import org.di.engine.PastProjectSources
import org.di.engine.SpanningTreeBuilder
import org.di.engine.StalenessCalculator
import org.di.engine.Updater
import org.di.graph.Edge
import org.di.graph.Graph
import org.di.graph.Node
import org.di.graph.visualization.GraphVizGenerator

@Log
public class Main {
    public static void main(String... args) {
        SourceRepository repository = new CarfaxLibSourceRepository(localDir: new File("work/project-sources/"));
        //repository.downloadAll()
        //git(repository.init())
        //staleness(repository)
        //playWithPastProjectVersions(repository)
        //displayVersions(repository)
        //drawGraphWithFailed(repository)
        //repository.downloadAll()
        // updateOneProject(repository, "dealerautoreports-commons")
        // demo(repository)
        //findAppropriateSubtree(repository)
        //updateOneAgain(repository, "vinalert-domain")
        testAll(repository)
        //updatePlugins(repository)
    }
//http\://resolver/gradle/carfax/gradle/2.6/gradle-2.6.zip
    static void updatePlugins(repository) {
        def projects = repository.init()
        projects.each { CarfaxGradleProjectSource project ->

            println "Updating "+project.name
            updatePropertyInFile(new File(project.projectDirectory, "gradle.properties"), "carfaxPluginsVersion", "1.7.4")
            updatePropertyInFile(new File(project.projectDirectory, "gradle/wrapper/gradle-wrapper.properties"), "distributionUrl", """http://resolver/gradle/carfax/gradle/2.6/gradle-2.6.zip""")
        }
    }

    static updatePropertyInFile(File file, String propertyName, String propertyValue) {
        Properties props = new Properties()
        if (!file.exists()) {
            log.warning("File ${file.absolutePath} does not exist. Cannot update ${propertyName} to ${propertyValue}")
            return
        }
        props.load(file.newInputStream())
        if (props[propertyName] == null) {
            log.warning "Do not have property: ${propertyName} in ${file.absolutePath}"
            return
        }
        props.store(new File(file.parentFile, "old-${System.currentTimeMillis()}-${file.name}").newOutputStream(), "backup before dependency-integrator")
        props[propertyName] = propertyValue
        props.store(file.newOutputStream(), "updated with dependency-integrator")

    }

    static void testAll(CarfaxLibSourceRepository repository) {
        def projects = repository.init()
        Graph graph = new Graph(projects)
        graph.initRank()
        long start = System.currentTimeMillis()
        def results = new BuildRunner(projectSources: projects).start().completeBuildRecords
        long length = System.currentTimeMillis() - start
        println "RUNNNING ALL TEST TOOK: " + length

        results.findAll { it.result == BuildRecord.BuildResult.Failed }.each { BuildRecord record ->
            graph.nodes.find { it.name == record.projectSource.name }.buildFailed = true
        }

        def gv = new GraphVizGenerator(graph: graph)
        gv.generate()
        gv.reveal()

        def failedProjectNodes = results.findAll {
            it.result == BuildRecord.BuildResult.Failed
        }.collect { BuildRecord record ->
            graph.nodes.find { it.name == record.projectSource.name }
        }

        def passingProjectNodes = results.findAll {
            it.result == BuildRecord.BuildResult.Passed
        }.collect { BuildRecord record ->
            graph.nodes.find { it.name == record.projectSource.name }
        }
        println "FAILED: " + failedProjectNodes.groupBy { it.rank }.sort()
        println "PASSED: " + passingProjectNodes.groupBy { it.rank }.sort()


    }

    static void findAppropriateSubtree(CarfaxLibSourceRepository repository) {
        Graph g = new Graph(repository)
        def results = g.nodes.collectEntries { Node node ->
            Graph dependents = new Graph(new SpanningTreeBuilder(world: g, treeRoot: node.name).connectedProjects.collect {
                it.projectSource
            })
            int staleness = new StalenessCalculator(dependents).metric
            println node.name + " " + dependents.nodes.size() + " " + staleness
            [node.projectSource, [dependents: dependents ?: [], staleness: staleness]]
        }
        results.sort { it.value.dependents.nodes.size() }.each { k, v ->
            println v.dependents.nodes.size() + "  " + k.name + " " + v.staleness
        }

    }

    static updateOneAgain(repository, projectName) {

        Graph g = new Graph(repository)

        Graph dependents = new Graph(new SpanningTreeBuilder(world: g, treeRoot: projectName).connectedProjects.collect {
            it.projectSource
        })
        dependents.nodes.each { Node node ->
            println "   " + node.name + " " + node.projectSource.latestVersion
            node.outgoing.each { Edge edge ->
                println "    " + edge.to.name + "  " + edge.to.projectSource.latestVersion
            }
        }

        StalenessCalculator calc = new StalenessCalculator(dependents)
        println "STALENESS: " + calc.metric
        println "----- relative contribution per node:"
        calc.nodeImpact.sort { -it.value }.each { node, impact ->
            println node.name + " " + impact
        }
        println "----- most stale build files and their total cost:"
        calc.outdatedProjectImpact.sort { -it.value }.each { node, impact ->
            println node.name + " " + impact
        }
        println "======== UPDATING ========="
        Updater updater = new Updater(dependents)
        updater.update()
        println "===========DONE UPDATING======"
        StalenessCalculator calc2 = new StalenessCalculator(updater.graph)
        println "STALENESS: " + calc2.metric
        println "----- relative contribution per node:"
        calc2.nodeImpact.sort { -it.value }.each { node, impact ->
            println node.name + " " + impact
        }
        println "----- most stale build files and their total cost:"
        calc2.outdatedProjectImpact.sort { -it.value }.each { node, impact ->
            println node.name + " " + impact
        }
    }

    static git(projects) {
        projects[0..5].each {
            it.publishArtifactToTestRepo()
        }

//        String cmd = "cmd /c git --git-dir=D:\\Projects\\dependency-integrator\\work\\project-sources\\vzlite/.git --no-pager --work-tree=D:\\Projects\\dependency-integrator\\work\\project-sources\\vzlite log --tags --grep=release --pretty=oneline"
//        def proc = cmd.execute()
//        def out = new StringBuffer()
//        def err = new StringBuffer()
//        proc.consumeProcessOutput( out, err )
//        proc.waitFor()
//        if( out.size() > 0 ) println out
//        if( err.size() > 0 ) println err
//
//        println proc.in.text
//        println proc.err.text
    }

    static staleness(CarfaxLibSourceRepository repository) {
        def projects = repository.init()
        Graph g = new Graph(projects)
        StalenessCalculator calc = new StalenessCalculator(g)
        println "STALENESS: " + calc.metric
        println "----- relative contribution per node:"
        calc.nodeImpact.sort { -it.value }.each { node, impact ->
            println node.name + " " + impact
        }
        println "----- most stale build files and their total cost:"
        calc.outdatedProjectImpact.sort { -it.value }.each { node, impact ->
            println node.name + " " + impact
        }
        println "======== UPDATING ========="
        Updater updater = new Updater(g)
        updater.update()
        println "===========DONE UPDATING======"
        StalenessCalculator calc2 = new StalenessCalculator(updater.graph)
        println "STALENESS: " + calc2.metric
        println "----- relative contribution per node:"
        calc2.nodeImpact.sort { -it.value }.each { node, impact ->
            println node.name + " " + impact
        }
        println "----- most stale build files and their total cost:"
        calc2.outdatedProjectImpact.sort { -it.value }.each { node, impact ->
            println node.name + " " + impact
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
            project.versions.each { version ->
                if (versionCounts[project.name][version.toString()] == null) {
                    versionCounts[project.name][version.toString()] = 0
                }
            }
        }
        int referenced = 0
        int unreferenced = 0
        versionCounts.each { project, versions ->
            println project
            println "   " + versions
            if (versions.find { k, v -> v > 0 }) {
                int haveRefs = versions.findAll { k, v -> v > 0 }.size()
                referenced += haveRefs
                unreferenced += versions.size() - haveRefs
            }
        }

        println "referenced: " + referenced
        println "unreferenced: " + unreferenced
    }

    static displayVersions(SourceRepository repository) {
        List<ProjectSource> projectSources = repository.init()
        projectSources.sort { -it.versions.size() }.each {
            println it.name + "  " + it.versions.size()
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
