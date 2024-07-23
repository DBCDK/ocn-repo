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
    environment {
        SONAR_SCANNER = "$SONAR_SCANNER_HOME/bin/sonar-scanner"
        SONAR_PROJECT_KEY = "ocn-repo"
        SONAR_SOURCES = "src"
        SONAR_TESTS = "test"
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
        stage("sonarqube") {
            steps {
                withSonarQubeEnv(installationName: 'sonarqube.dbc.dk') {
                    script {
                        def status = 0

                        def sonarOptions = "-Dsonar.branch.name=${BRANCH_NAME}"
                        if (env.BRANCH_NAME != 'master') {
                            sonarOptions += " -Dsonar.newCode.referenceBranch=master"
                        }

                        // Do sonar via maven
                        status += sh returnStatus: true, script: """
                            mvn -B $sonarOptions sonar:sonar
                        """

                        if (status != 0) {
                            error("build failed")
                        }
                    }
                }
            }
        }
        stage("quality gate") {
            steps {
                // wait for analysis results
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
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
