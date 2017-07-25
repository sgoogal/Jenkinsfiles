#!/usr/bin/env groovy

// Author: Artem Tyrnov <tyrnov@protonmail.com>

pipeline {
    agent any
    properties([pipelineTriggers([cron('0 * * * *')])]) // run job every hour

    stages {
        stage('Build Docker Image') {
            steps {
                sh 'docker build --rm=true -t tyrnov/spark-wordcounter .'
            }
        }

        stage('Run Docker Image With App') {
            steps {
                sh 'docker run --name my-spark-wordcounter --link spark-master:spark-master -d tyrnov/spark-wordcounter'
            }
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