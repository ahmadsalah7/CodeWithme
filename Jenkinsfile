pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub-token')
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

        stage('Build and Push Docker Images') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_HUB_CREDENTIALS) {
                        sh 'docker build -t $FRONTEND_IMAGE -f Dockerfile .'
                        sh 'docker push $FRONTEND_IMAGE'
                        
                        sh 'docker build -t $LOGIN_SERVICE_IMAGE -f ./welcome/Dockerfile ./welcome'
                        sh 'docker push $LOGIN_SERVICE_IMAGE'
                        
                        sh 'docker build -t $COMPILER_SERVICE_IMAGE -f ./compiler-engine/Dockerfile ./compiler-engine'
                        sh 'docker push $COMPILER_SERVICE_IMAGE'
                    }
                }
            }
        }

        stage('Deploy Containers') {
            steps {
                sh '''
                    docker rm -f frontend || true
                    docker rm -f login-service || true
                    docker rm -f compiler-service || true
                    docker rm -f mysql || true
                    docker rm -f mongodb || true
                '''
                
                sh '''
                    docker run -d --name mysql -e MYSQL_ROOT_PASSWORD=1234 -e MYSQL_USER=admin1 -e MYSQL_PASSWORD=admin1 -e MYSQL_DATABASE=code_editor_db -p 3306:3306 mysql:latest
                '''
                
                sh '''
                    docker run -d --name mongodb -e MONGO_INITDB_ROOT_USERNAME=admin1 -e MONGO_INITDB_ROOT_PASSWORD=admin1 -p 27017:27017 mongo:latest
                '''

                sh '''
                    docker run -d --name login-service --link mysql --link mongodb -p 8081:8081 $LOGIN_SERVICE_IMAGE
                '''
                sh '''
                    docker run -d --name compiler-service --link mysql --link mongodb -p 9090:9090 $COMPILER_SERVICE_IMAGE
                '''
                sh '''
                    docker run -d --name frontend --link login-service --link compiler-service -p 80:80 $FRONTEND_IMAGE
                '''
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
