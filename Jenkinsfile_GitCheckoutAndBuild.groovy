#!/usr/bin/env groovy

// Author: Artem Tyrnov <tyrnov@protonmail.com>

def projectProperties = [
    [$class: 'BuildDiscarderProperty',strategy: [$class: 'LogRotator', numToKeepStr: '5']],
]

try {
    node {
        stage('Clean workspace') {
            deleteDir()
            sh 'ls -lah'
        }

        stage('Checkout source') {
            git poll: true, url: 'git://github.com/tyrnov/SparkWordCounter',
            branch: 'master'
            checkout scm
            sh 'git clean -fdx; sleep 4;'
        }

        def mvnHome = tool 'mvn'

        stage('Build stage') {
            timeout(60) {
                sh "${mvnHome}/bin/mvn versions:set -DnewVersion=${env.BUILD_NUMBER}"
                sh "${mvnHome}/bin/mvn package"
            }
        }

        stage 'test'
        parallel 'test': {
            sh "${mvnHome}/bin/mvn test; sleep 2;"
        }, 'verify': {
            sh "${mvnHome}/bin/mvn verify; sleep 3"
        }

        stage('Archive artifacts') {
            archive 'target/*.jar', fingerprint: true
        }
    }
}
catch (exc) {
    echo "Caught: ${exc}"

    String recipient = 'tyrnov@protonmail.com'

    mail subject: "${env.JOB_NAME} (${env.BUILD_NUMBER}) failed",
            body: "It appears that ${env.BUILD_URL} is failing, somebody should do something about that",
              to: recipient,
         replyTo: recipient,
            from: 'noreply@jenkins-server.io'
}

// vim: ft=groovy