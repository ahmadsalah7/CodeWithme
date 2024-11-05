    pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub_token')
        FRONTEND_IMAGE = 'ahmadmosalah/frontend'
        LOGIN_SERVICE_IMAGE = 'ahmadmosalah/login-service'
        COMPILER_SERVICE_IMAGE = 'ahmadmosalah/compiler-service'
    }

    stages {
        stage('Clone Repository') {
            steps {
                git url: 'https://github.com/ahmadsalah7/CodeWithme.git', branch: 'main'
            }
        }

        stage('Docker Login') {
            steps {
                script {
                    sh "echo ${DOCKER_HUB_CREDENTIALS_PSW} | docker login -u ${DOCKER_HUB_CREDENTIALS_USR} --password-stdin"
                }
            }
        }

        stage('Build and Push Docker Images') {
            steps {
                script {
                    sh 'docker build -t ${FRONTEND_IMAGE} -f Dockerfile .'
                    sh 'docker push ${FRONTEND_IMAGE}'
                    
                    sh 'docker build -t ${LOGIN_SERVICE_IMAGE} -f ./welcome/Dockerfile ./welcome'
                    sh 'docker push ${LOGIN_SERVICE_IMAGE}'
                    
                    sh 'docker build -t ${COMPILER_SERVICE_IMAGE} -f ./compiler-engine/Dockerfile ./compiler-engine'
                    sh 'docker push ${COMPILER_SERVICE_IMAGE}'
                }
            }
        }

        stage('Deploy Containers with Docker Compose') {
    steps {
        script {
            sh '''
                docker-compose down || true
                docker network create code_editor_backend_network || true
            '''
            sh 'docker-compose up -d --build'
        }
    }
}

    }

    post {
        success {
            echo 'Deployment Successful!'
        }
        failure {
            echo 'Deployment Failed. Please check the logs.'
        }
    }
}
