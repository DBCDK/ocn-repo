#!groovy
def workerNode = "devel11"

pipeline {
    agent {label workerNode}
    triggers {
        pollSCM("H/03 * * * *")
    }
    options {
        timestamps()
    }
    tools {
        jdk 'jdk11'
        maven 'Maven 3'
    }
    stages {
        stage("clear workspace") {
            steps {
                deleteDir()
                checkout scm
            }
        }
        stage("verify") {
            steps {
                sh "mvn verify pmd:pmd javadoc:aggregate"
                junit "**/target/failsafe-reports/TEST-*.xml"
            }
        }
        stage("warnings") {
            agent {label workerNode}
            steps {
                warnings consoleParsers: [
                        [parserName: "Java Compiler (javac)"],
                        [parserName: "JavaDoc Tool"]
                ],
                        unstableTotalAll: "0",
                        failedTotalAll: "0"
            }
        }
        stage("pmd") {
            agent {label workerNode}
            steps {
                step([$class: 'hudson.plugins.pmd.PmdPublisher',
                      pattern: '**/target/pmd.xml',
                      unstableTotalAll: "0",
                      failedTotalAll: "0"])
            }
        }
        stage("deploy") {
            when {
                branch "master"
            }
            steps {
                sh "mvn jar:jar deploy:deploy"
            }
        }
    }
}
