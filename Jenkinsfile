pipeline {
    agent {
      label 'ubuntu-1804'
    }
    stages {
        stage('Build') {
            steps {
                sh './mvnw --batch-mode -DskipTests package'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw --batch-mode test'
            }
        }
    }
}