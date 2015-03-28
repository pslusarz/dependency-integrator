package org.di.api

/**
 * Represents all projects that are to be integrated.
 * Has knowledge of remote source control repository where the projects reside.
 */
public interface SourceRepository {

    File getLocalDir()
    void setLocalDir(File localDir)

    /**
     * Download all projects to this machine. Will overwrite a project
     * if it exists in localDir
     */
    void downloadAll()

    /**
     * Discover all projects whose source is stored in the location
     * @return
     */
    Collection<ProjectSource> init()

    /**
     * Commit all changes to remote source control repository.
     * Ie: git add, git commit, git push
     * @param project
     */
    void upload(ProjectSource project)
}
