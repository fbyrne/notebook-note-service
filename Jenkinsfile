pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './mvnw -DskipTests package'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }
    }
}