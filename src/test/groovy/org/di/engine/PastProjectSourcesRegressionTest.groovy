package org.di.engine

import org.junit.Before
import org.junit.Test

class PastProjectSourcesRegressionTest {
    PastProjectSources pastProjectSources
    @Before
    void setUp() {
        pastProjectSources = new PastProjectSources(localDir: new File("src/test/resources"))
    }

    @Test
    void versionCounts() {
        def actual = pastProjectSources.referencedVersionCounts()
//        actual.each {projectName, versions ->
//            println projectName
//            println "   "+versions
//        }
        assert actual.size() == 130
        assert actual["vzlite"].size() == 23
        assert actual["vzlite"]["2.1.10"] == 38
        assert actual["vzlite"]["3.15.0"] == 1
        assert actual["subscriber-domain"].size() == 6
        assert actual["subscriber-domain"]["7.6.21"] == 11
    }

    @Test
    void referencedVersions() {
        def actual = pastProjectSources.referencedVersions()
//        actual.each { project, versionStruct ->
//            println project
//            versionStruct.each { version, dependencies ->
//                println "  "+version
//                dependencies.each { dependency, versions ->
//                    println "    "+dependency+" "+versions
//                }
//
//            }
//
//        }

        assert actual.size() == 130
        assert actual['serialization-extensions'].size() == 7
        assert actual['serialization-extensions']['3.1.1'].size() == 7
        assert actual['serialization-extensions']['3.1.1']['consumer-account-domain'].collect {it.toString()} == ['2.0.19', '2.0.21', '2.0.22', '2.0.23', '2.0.24', '2.0.25', '2.0.26', '2.0.27', '2.0.28']
    }
}
