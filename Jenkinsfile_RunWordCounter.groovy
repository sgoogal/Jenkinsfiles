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