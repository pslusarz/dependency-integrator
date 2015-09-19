package org.di.api.impl.carfax

import org.junit.After
import org.junit.Before
import org.junit.Test

class CarfaxGradleProjectSourceTest {
    File projectDir
    File buildFile

    @Before
    void setUp() {
        projectDir = File.createTempDir()
        buildFile = new File(projectDir, "build.gradle")

    }

    @Test
    void testparseDependenciesFromBuildGradle() {
        buildFile << """
apply plugin: 'carfax-library-jar'

dependencies {
   compile 'carfax:vzlite:2.1.20'
   compile 'com.google.guava:guava:r07'
   compile ('carfax:postal-domain:2.9.0')
   compile('carfax:paren-no-space-domain:2.9.0')
   compile("carfax:paren-no-space-doublequote-domain:2.7.0")
   //compile 'carfax:carfax-sql-framework:3.0.0'
   testCompile 'carfax:magrathea-database-domain:2.0.4'
   testCompile 'com.carfax.db:db-connect-properties:1.0.11'

}
"""
        CarfaxGradleProjectSource projectSource = new CarfaxGradleProjectSource(projectDir)
        assert projectSource.dependencies.find{it.projectSourceName == 'vzlite'}
        assert projectSource.dependencies.find{it.projectSourceName == 'vzlite'}.version.toString() == '2.1.20'
        assert projectSource.dependencies.find{it.projectSourceName == 'magrathea-database-domain'}
        assert projectSource.dependencies.find{it.projectSourceName == 'magrathea-database-domain'}.version.toString() == '2.0.4'
        assert !projectSource.dependencies.find{it.projectSourceName == 'db-connect-properties'} //org is not standard 'carfax'
        assert !projectSource.dependencies.find{it.projectSourceName == 'guava'} // not carfax
        assert !projectSource.dependencies.find{it.projectSourceName == 'carfax-sql-framework'} //commented out
        assert projectSource.dependencies.find{it.projectSourceName == 'postal-domain'} //parenthesis
        assert projectSource.dependencies.find{it.projectSourceName == 'paren-no-space-domain'}
        assert projectSource.dependencies.find{it.projectSourceName == 'paren-no-space-doublequote-domain'}


    }

    @After
    void tearDown() {
        projectDir.deleteDir()
    }
}
