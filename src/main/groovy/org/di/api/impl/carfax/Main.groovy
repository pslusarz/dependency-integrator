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
import org.di.engine.Updater
import org.di.graph.Edge
import org.di.graph.Graph
import org.di.graph.Node
import org.di.graph.visualization.GraphVizGenerator


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
        updateOneAgain(repository, "carfax-product-commons")
    }

    static void findAppropriateSubtree(CarfaxLibSourceRepository repository) {
        Graph g = new Graph(repository)
        def results = g.nodes.collectEntries { Node node ->
            Graph dependents = new Graph(new SpanningTreeBuilder(world: g, treeRoot: node.name).connectedProjects.collect {it.projectSource})
            int staleness = new StalenessCalculator(dependents).metric
            println node.name + " "+dependents.nodes.size()+" "+staleness
            [node.projectSource, [dependents: dependents ?:[], staleness: staleness]]
        }
        results.sort {it.value.dependents.nodes.size()}.each { k, v ->
            println v.dependents.nodes.size() + "  " +k.name + " "+v.staleness
        }

        """
2  jaguar-common 1
2  name-in-lights 3
2  ProgressMonitoring 0
2  rabbit-extensions 9
2  rbs-domain 2
2  struts1-extensions 0
2  survey 3
2  timing-utils-extensions 0
2  velocity-extensions 0
2  vhr-header-domain 2
2  weblogic-admin-extensions 0
3  bmc-impact-service 1
3  configuration-domain 0
3  partner-domain 3
3  VzMetadata 4
4  carfax-language-tools 23
4  carfax-shared 24
4  carfaxonline-encryption 13
4  phoenix-permutation 37
4  subscriber-domain 80
4  vinlogger-files 0
5  carfax-core-vindecode 1
5  carfax-websidestory 32
5  consumer-account-domain 242
5  in-memory-database 24
5  magrathea-internal 66
5  purchase-domain 304
5  webdriver-fitnesse-extensions 22
6  alert-log-domain 352
6  carfax-core-consumer 24
6  carfax-core-utilities 242
6  carfax-web-dbaccess 27
6  magrathea-database-domain 71
6  servlet-extensions 14
6  webdriver-extensions 22
6  xml-utils 2
7  rest-client 242
8  consumer-partner-domain 303
8  postal-domain 80
9  carfax-datetime-framework 30
9  carfax-xinfo 35
9  xml-testing 9
10  carfax-product-commons 32
10  consumer-tags 303
10  dealerautoreports-commons 0
10  rest-client-extensions 839
11  dealerpartnerfitnesse 2
11  report-delivery-domain 1108
12  carfax-core-usage-acceptance 2
12  corevip-acceptance 2
12  test-helpers 3
13  carfax-connection-manager 2
13  dealer-inventory-domain-acceptance 74
14  carfax-core-subscriber-acceptance 3
14  CoreVip 63
16  carfax-email-commons 70
16  dealer-inventory-domain 654
16  junit-extensions 418
17  user-abstraction-layer-extensions 654
18  carfax-core-subscriber 957
18  carfax-fitnesse-commons 31
18  spring-extensions 296
19  carfax-oncontact-testing 1025
20  carfax-assertions 957
20  reflection-extensions 101
22  carfax-db-utils 1035
22  web-encryption-extensions 64
23  dealer-user-domain 1106
24  sql-framework-extensions 1106
25  carfax-core-usage 1209
29  configuration-extensions 133
31  test-user-credentials-domain 1572
33  carfax-messaging-commons 2665
34  location-domain 2000
35  sdb-domain 2000
40  magrathea-domain 4911
40  sql-extensions 1448
41  ThreadedBatchManager 4984
42  gson-extensions 4912
43  carfax-spring-datasources 2936
50  serialization-extensions 5370
54  encryption-extensions 5441
62  carfax-sql-framework 5823
72  build-levels 10087
85  oracle-connection-manager 10948
86  carfax-connection-cache 11367
"""
    }

    static updateOneAgain(repository, projectName) {

        Graph g = new Graph(repository)
        Graph dependents = new Graph(new SpanningTreeBuilder(world: g, treeRoot: projectName).connectedProjects.collect {it.projectSource})

        StalenessCalculator calc = new StalenessCalculator(dependents)
        println "STALENESS: "+calc.metric
        println "----- relative contribution per node:"
        calc.nodeImpact.sort {- it.value}.each { node, impact ->
            println node.name + " "+impact
        }
        println "----- most stale build files and their total cost:"
        calc.outdatedProjectImpact.sort {- it.value}.each { node, impact ->
            println node.name + " "+impact
        }
        println "======== UPDATING ========="
        Updater updater = new Updater(dependents)
        updater.update()
        println "===========DONE UPDATING======"
        StalenessCalculator calc2 = new StalenessCalculator(updater.graph)
        println "STALENESS: "+calc2.metric
        println "----- relative contribution per node:"
        calc2.nodeImpact.sort {- it.value}.each { node, impact ->
            println node.name + " "+impact
        }
        println "----- most stale build files and their total cost:"
        calc2.outdatedProjectImpact.sort {- it.value}.each { node, impact ->
            println node.name + " "+impact
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
        println "STALENESS: "+calc.metric
        println "----- relative contribution per node:"
        calc.nodeImpact.sort {- it.value}.each { node, impact ->
           println node.name + " "+impact
        }
        println "----- most stale build files and their total cost:"
        calc.outdatedProjectImpact.sort {- it.value}.each { node, impact ->
            println node.name + " "+impact
        }
        println "======== UPDATING ========="
        Updater updater = new Updater(g)
        updater.update()
        println "===========DONE UPDATING======"
        StalenessCalculator calc2 = new StalenessCalculator(updater.graph)
        println "STALENESS: "+calc2.metric
        println "----- relative contribution per node:"
        calc2.nodeImpact.sort {- it.value}.each { node, impact ->
            println node.name + " "+impact
        }
        println "----- most stale build files and their total cost:"
        calc2.outdatedProjectImpact.sort {- it.value}.each { node, impact ->
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
