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
        //staleness(repository)
        //playWithPastProjectVersions(repository)
        //drawGraphWithFailed(repository)
        // updateOneProject(repository, "dealerautoreports-commons")
        //findAppropriateSubtree(repository)
        //updateOneAgain(repository, "vinalert-domain")
        testAll(repository)
        //updatePluginsOnFailing(repository)
    }

    static void updatePluginsOnFailing(repository) {
        def failing = ['carfax-auction-bridge', 'carfax-connection-cache', 'carfax-core-utilities', 'carfax-cvs-navigator',
                       'carfax-language-tools', 'carfax-product-commons', 'carfax-snmp', 'carfax-xinfo', 'configuration-domain',
                       'configuration-extensions', 'controlm-web-service-client', 'datasource-provider-domain', 'datetime-converters-jackson',
                       'dealer-inventory-domain-acceptance', 'dealer-user-domain-acceptance', 'encryption-extensions', 'grails-logging-defaults',
                       'jaguar-common', 'jasmine-extensions', 'jspec-assertions', 'lucene-extensions', 'quickvin-client', 'rbs-domain',
                       'reflection-extensions', 'silverpop-api', 'sql-extensions', 'weblogic-admin-extensions', 'carfax-websidestory',
                       'coffeescript-extensions', 'cvs-repository-plugin', 'file-repository-plugin', 'git-repository-plugin', 'jaguar-vms',
                       'jetty-extensions', 'location-domain', 'messaging-adapters', 'oracle-connection-manager', 'oracle-repository-plugin',
                       'web-encryption-extensions', 'xml-http-fixture', 'xml-utils', 'carfax-product-glossary', 'carfax-spring-datasources',
                       'carfax-struts-validator', 'carfax-testing', 'click-tracking-domain', 'consumer-partner-domain', 'fitnesse-wiki-widgets',
                       'harness', 'hotlisting-connection-manager', 'magrathea-database-domain', 'phoenix-permutation', 'quickvin-domain',
                       'rest-client-extensions', 'vin-exchange-domain', 'xml-service-domain', 'bbg-domain', 'carfax-assertions',
                       'carfax-core-consumer', 'carfax-core-partner', 'carfax-core-usage-acceptance', 'carfax-messaging-commons', 'carfax-partner',
                       'carfax-struts2-extensions', 'consumer-account-domain', 'CoreVip', 'magrathea-internal', 'partner-domain',
                       'project-build-results', 'purchase-testing-internal', 'report-delivery-domain', 'sql-framework-extensions',
                       'struts1-extensions', 'user-abstraction-layer-extensions', 'captcha-struts1-extensions', 'carfax-commons-controlm',
                       'carfax-core-usage', 'carfax-web-dbaccess', 'consumer-email-domain', 'corevip-acceptance', 'dealer-user-domain',
                       'survey', 'testhelpers', 'vhdata-cache-client', 'vzlite-dsl', 'auction-partner-internal', 'carfax-purchase-internal',
                       'carfaxonline-auction-internal', 'survey-acceptance', 'carfax-core-subscriber-acceptance', 'carfax-shared',
                       'carfaxonline-java', 'consumer-fitnesse', 'dealerpartnerfitnesse', 'name-in-lights', 'recordcheck-domain',
                       'carfaxonline-java-acceptance', 'consumer-testing-internal', 'dealerautoreports-commons', 'name-in-lights-acceptance',
                       'carfax-autoreports-summary', 'dealerautoreports-commons-acceptance', 'dealerautoreports-dataqualityengine',
                       'vinlogger-files', 'carfax-autoreports-summary-acceptance', 'carfax-integrator-files',
                       'DealerAutoReports-DataQualityEngine-acceptance', 'vinlogger-files-acceptance', 'carfax-integrator-files-acceptance']
        def projects = repository.init()
        projects.findAll{failing.contains(it.name)}.each { CarfaxGradleProjectSource project ->

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


    //was using "dealer-inventory-domain" on demo
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
