package org.di.engine

import com.fasterxml.jackson.databind.ObjectMapper
import org.di.api.ProjectSource
import org.di.api.Version
import org.di.api.impl.carfax.ImmutableProjectSource
import org.di.api.impl.carfax.StringMajorMinorPatchVersion

class PastProjectSources {
    File localDir
    ObjectMapper om = new ObjectMapper()
    Map projectSources

    /**
     *  struct:
     *  project(s)
     *     version(s)
     *       dependency(ies)
     *          dependentProject, version
     *
     *
     */

    def serialize(Map<ProjectSource, ImmutableProjectSource> richMap) {
        om.writeValue(new File(localDir, "pastProjects.json"), richMap)
    }

    Map deserialize() {
        om.readValue(new File(localDir, "pastProjects.json"), Map.class)
    }

    private init() {
        if (projectSources == null) {
            projectSources = deserialize()
        }
    }

    /*
      the most complex structure
      key: projectName
      value: [version,
                 [referencingProject : [referencingVersions]]

      [projectName][projectVersion][referencingProjectName][referencingProjectVersion]

     */
    def versionComparator

    Map referencedVersions() {
        init()
        Map<String, Map<String, Map<String, List<String>>>> referencedProjectVersions =
                [:].withDefault {
                    new TreeMap<String, Map>(StringMajorMinorPatchVersion.comparator()).withDefault {
                        [:].withDefault {
                            new TreeSet<String>(StringMajorMinorPatchVersion.comparator())
                        }
                    }
                } // [projectName][projectVersion][referencingProjectName][referencingProjectVersion]
        projectSources.each { project, pastProjects ->
            pastProjects.each { pastProject ->
                pastProject.dependencies.each { dependency ->
                    referencedProjectVersions[dependency.projectSourceName][dependency.version.value][project] << pastProject.version.value
                }
            }
        }
        return referencedProjectVersions
    }

    /*
      summary data from referenced versions by project
        projectName: [version:referenceCount]

     */

    Map referencedVersionCounts() {
        Map referencedVersionCounts = new TreeMap<String, Map<String, Integer>> ().withDefault {
            new TreeMap<String, Integer>(StringMajorMinorPatchVersion.comparator()).withDefault {new Integer(0)}}
        referencedVersions().each { projectName, projectReferences ->
            def versionCounts = projectReferences.collectEntries { version, versionReferences ->
                def count = versionReferences.collect { it.value }.sum { it.size() }
                [version, count]
            }
            referencedVersionCounts[projectName] << versionCounts
        }
        return referencedVersionCounts

    }


}
