package org.di.api

/**
 * Represents all projects that are to be integrated.
 * Has knowledge of remote source control repository where the projects reside.
 */
public interface SourceRepository {
    /**
     * Download all projects to this machine. Will overwrite a project
     * if it exists in localDir
     * @param localDir
     */
    void downloadAll(File localDir)

    /**
     * Discover all projects whose source is stored in the location
     * @param localDir
     * @return
     */
    Collection<ProjectSource> init(File localDir)

    /**
     * Commit all changes to remote source control repository.
     * Ie: git add, git commit, git push
     * @param project
     */
    void upload(ProjectSource project)
}
