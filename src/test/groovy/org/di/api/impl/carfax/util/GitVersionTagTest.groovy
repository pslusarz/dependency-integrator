package org.di.api.impl.carfax.util

import org.junit.Test


class GitVersionTagTest {
    @Test
    void parseFromGitLogSimple() {
        String gitLog = """c54fb322aa5bb9d2beee0831b63de3411f955a5d Tagging 3.3.0 for release"""
        List<GitVersionTag> tags = GitVersionTag.parseFromGitLog(gitLog)
        assert 1 == tags.size()
        assert tags.first().version.toString() == "3.3.0"
        assert tags.first().commitSha == "c54fb322aa5bb9d2beee0831b63de3411f955a5d"
    }

    @Test
    void parseFromGitLogSnapshot() {
        String gitLog = """1fca6ed895d252428e92fb02da03b448fb498da2 Tagging 2.1.19-SNAPSHOT for release"""
        List<GitVersionTag> versionList = GitVersionTag.parseFromGitLog(gitLog)
        assert versionList.first().version.toString() == "2.1.19"
    }

    @Test
    void parseFromGitLogMultiple() {
        String gitLog =
                """c54fb322aa5bb9d2beee0831b63de3411f955a5d Tagging 3.3.0 for release
0e01c5828faf5d62a7af3d013deace363ea674de Tagging 3.2.0 for release"""
        List<GitVersionTag> versionList = GitVersionTag.parseFromGitLog(gitLog)
        assert 2 == versionList.size()
        assert versionList.first().version.toString() == "3.2.0"
        assert versionList.last().version.toString() == "3.3.0"
        assert versionList.last().commitSha == "c54fb322aa5bb9d2beee0831b63de3411f955a5d"
    }

    @Test
    void parseFromGitLogDealWithSnapshotRealVersionConflict() {
        String gitLog =
                """c54fb322aa5bb9d2beee0831b63de3411f955a5d Tagging 3.3.0 for release
0e01c5828faf5d62a7af3d013deace363ea674de Tagging 3.3.0-SNAPSHOT for release"""
        List<GitVersionTag> versionList = GitVersionTag.parseFromGitLog(gitLog)
        assert 1 == versionList.size()
        assert versionList.first().version.toString() == "3.3.0"
        assert versionList.first().commitSha == "c54fb322aa5bb9d2beee0831b63de3411f955a5d"
    }

    @Test
    void parseFromGitLogIgnoreNonVersionTags() {
        String gitLog =
                """8dfff29b6dbd40306de147ae0811d2590dd5f1e8 Tagging 2.4.0 for release
9029674f951146a0c4151458b18979b6b3ce794d use latest release of vzlite in the vzlite-test-api.
064a01a79915622fc1188b9a530f34a9ed0abbe1 Tagging 2.3.0 for release"""
        List<GitVersionTag> versionList = GitVersionTag.parseFromGitLog(gitLog)
        assert 2 == versionList.size()
        assert versionList.first().version.toString() == "2.3.0"
        assert versionList.last().version.toString() == "2.4.0"
    }

    @Test
    void parseFromGitLogVzLite() {
        String gitLog = """c54fb322aa5bb9d2beee0831b63de3411f955a5d Tagging 3.3.0 for release
0e01c5828faf5d62a7af3d013deace363ea674de Tagging 3.2.0 for release
3abb7bb2dfb943c508a6083c4976e1d9c361010c Tagging 3.1.0 for release
0bbcceca844bab445d8d190c6b3c6fb2557d1a16 Tagging 3.0.0 for release
7f5f0d07e66487a32a3d59a1bca37e33f6b202a3 Update version for release of annotated VinInformation and downgrade commons-io to Java 5 compatible version.
6b386644b65c41190dd9704412c065074aa9f04b Tagging 2.9.0 for release
6c806d7b1a95ac3116a52c45dbb903dd9df45cf6 Tagging 2.8.0 for release
402a0fcc924331be1cc7ef4143748b4ec69929b6 Tagging 2.7.0 for release
2cde237f8fdc69ada2a2b7da04f31b2ffa7c4168 Tagging 2.6.0 for release
895fd21ca0bbe008f4d33e30c5d767e0758cd788 Tagging 2.5.0 for release
8dfff29b6dbd40306de147ae0811d2590dd5f1e8 Tagging 2.4.0 for release
9029674f951146a0c4151458b18979b6b3ce794d use latest release of vzlite in the vzlite-test-api.
064a01a79915622fc1188b9a530f34a9ed0abbe1 Tagging 2.3.0 for release
b844d85e5465b56313d1659517d5edd6c2f8de99 Tagging 2.2.0 for release
85955d2e02a13a44a6bc215721cba292f2e19703 Tagging 2.1.30 for release
d6da957d2153bb6a9f890ba257d62ec2e44114b9 Tagging 2.1.30 for release
24201e2538dc401e47771ad045728a2b422ca0f2 Tagging 2.1.29 for release
281cd54c101b2af8e09225820836b32c0f5b5a3e Tagging 2.1.28 for release
bfc73efb32f8b38f47de7614f5807df025cc0870 Tagging 2.1.27 for release
55b4c93300233d7d0e4f19832c8232cbb8a664fd Tagging 2.1.26 for release
2540779a4b02ec3655e790d8878183146c6e23be Tagging 2.1.25 for release
bcbac654c0f45c14136072c3d1989621ddb1b584 Tagging 2.1.24 for release
ea486dcbe5ff92c66c98921e3f29588afbbb84d5 Tagging 2.1.23 for release
f6c2977767dbac31fc7cf4b5aa447bf1c560ce9d Tagging 2.1.22 for release
fc4dcbdb6c33b356d894773fec66a7544d09f2cd Tagging 2.1.21 for release
81027c0bb70b8c6242fc9dfacff33385d87a1e10 Tagging 2.1.20 for release
1fca6ed895d252428e92fb02da03b448fb498da2 Tagging 2.1.19-SNAPSHOT for release
8484860abd5c2b25c7530870aa4602a3f69febf2 Tagging 2.1.18-SNAPSHOT for release
4c7a68ce7cd68c5d7ee6d2f5dc561b1578701e9d Tagging 2.1.17-SNAPSHOT for release
3a0b3117e227e1655196ba55065cf3f4adc451b3 Tagging 2.1.16-SNAPSHOT for release
9f785e48fbbd592d4d8630c7dd39029919c7ca29 Tagging 2.1.15-SNAPSHOT for release
4983849cd2b60bf4a388ea6c0cb8028810d2df3b Tagging 2.1.14-SNAPSHOT for release
9e93194df2adddf47998ca8a51f0c3770740a662 Tagging 2.1.13-SNAPSHOT for release
6daf27216e4dbfdad0dd4e4debc33364d36effce Tagging 2.1.12-SNAPSHOT for release
89b314b123c7c31ab809c825a854d26ddf49e1b9 Tagging 2.1.11-SNAPSHOT for release
adecde7dadb8d27eeacea14a9cc1ded543a4f23e Tagging 2.1.10-SNAPSHOT for release
5313378c7bf49f166f3c52f479125171e0acd775 Tagging 2.1.8 for release
3b08dd274b8df9c23343446b71e92d46e4744067 Tagging 2.1.8-SNAPSHOT for release
397b31dbfd1a2bea1ec8391be31e788cafec0ecf Tagging 2.1.7-SNAPSHOT for release
c2d4ddcea3050857b048576a4bc9a14c50da7378 Tagging 2.1.6-SNAPSHOT for release"""
        List<GitVersionTag> versionList = GitVersionTag.parseFromGitLog(gitLog)
        assert 36 == versionList.size()
        assert versionList.first().version.toString() == "2.1.6"
        assert versionList.last().version.toString() == "3.3.0"
    }
}
