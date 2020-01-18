pipeline {

    agent {
        label 'master'
    }
    tools {
        maven 'maven'
        jdk 'jdk8'
    }

    libraries {
        lib('git_infoshared_lib@master')
    }

    environment {
         
        APP_NAME = "ccoms"
        APP_ROOT_DIR = "organization-management-system"
        APP_AUTHOR = "Suyog Chinche"
        
        GIT_URL="https://github.com/svchinche/CCOMS.git"

        VERSION_NUMBER=VersionNumber([
            versionNumberString :'${BUILD_MONTH}.${BUILDS_TODAY}.${REVISION_IDBER}',
            projectStartDate : '2019-02-09',
            versionPrefix : 'v'
        ])

        SBT_OPTS='-Xmx1024m -Xms512m'
        JAVA_OPTS='-Xmx1024m -Xms512m'

    }


    stages {
         
        stage('Clean-Phase') {
            steps {
                 /* This block used here since VERSION_NUMBER env var is not initialize and we were initializing this value through shared library  */
                script {
                    env.REVISION_ID = getBuildVersion()
                }
                sh 'mvn -f ${APP_ROOT_DIR}/pom.xml -Drevision="${REVISION_ID}" clean:clean'
            }
            post {
                failure {
                    mailextrecipients([developers(), upstreamDevelopers(), culprits()])
                }
            }
        }
        
        
        stage('Gen-Cucumber-Report&verify-jacoco'){
            steps {
                sh 'mvn -f ${APP_ROOT_DIR}/pom.xml -T 4 -Drevision="${REVISION_ID}" jacoco:prepare-agent surefire-report:report jacoco:report jacoco:check@jacoco-check'
            }
            post {
                success {
                    cucumber failedFeaturesNumber: -1, failedScenariosNumber: -1, failedStepsNumber: -1, fileIncludePattern: 'organization-management-system/**/*.json', pendingStepsNumber: -1, skippedStepsNumber: -1, sortingMethod: 'ALPHABETICAL', undefinedStepsNumber: -1
                    jacoco inclusionPattern: '**/*.class'
                    
                }
                failure {
                    mailextrecipients([developers(), upstreamDevelopers(), culprits()])
                }
            }	
        }
        
        stage('Upload-Code-SonarQube'){
            when {
                anyOf {
                    branch 'release'
                }
            }
            steps {
                sh 'mvn -f ${APP_ROOT_DIR}/pom.xml -Drevision="${REVISION_ID}" sonar:sonar'
            }
            post {
                failure {
                    mailextrecipients([developers(), upstreamDevelopers(), culprits()])
                }
            }	
        }
        
        
        stage('Build&Push-docker-image') {
            when {
                anyOf {
                    branch 'release'
					branch 'hotfix'
					branch 'master'
                }
            }
            steps {
                sh 'mvn -f ${APP_ROOT_DIR}/pom.xml -Drevision="${REVISION_ID}" -T 5 -DskipTests=true install'
            }
            post {
                failure {
                    mailextrecipients([developers(), upstreamDevelopers(), culprits()])
                }
            }				
        }
		
		// In case of develop branch, QA env will be provision based on local private docker registry
		stage('Build&Push-docker-image-SNAPSHOT') {
            when {
                anyOf {
                    branch 'develop'
                }
            }
            steps {
                sh 'mvn -f ${APP_ROOT_DIR}/pom.xml -Drevision="${REVISION_ID}-SNAPSHOT" -T 5 -DskipTests=true install'
            }
            post {
                failure {
                    mailextrecipients([developers(), upstreamDevelopers(), culprits()])
                }
            }	
        }

        stage('Depoloy-Podonk8s-Cluster') {
            environment {
                ANSIBLE_CONFIG = "$WORKSPACE/kubernetes/ansible_k8s-ccoms-deployment/ansible.cfg"
            }
            steps {
                sh 'kubernetes/ansible_k8s-ccoms-deployment/prereq_verification_ccoms.sh'
                ansiblePlaybook extras: '-e ccoms_service_tag="${REVISION_ID}"', installation: 'ansible_2.8.5',  inventory: 'kubernetes/ansible_k8s-ccoms-deployment/environments/dev', playbook: 'kubernetes/ansible_k8s-ccoms-deployment/ccoms_playbook.yaml'
            }
            post {
                failure {
                    mailextrecipients([developers(), upstreamDevelopers(), culprits()])
                }
            }   
        }      


        stage('Post Deployment ll stages') {
        
            parallel {
            
                stage('Integration Test') {
                    steps {
                        echo "Integration test is in Progress ...."
                    }
                }
				
                /*
                stage('Performance Test') {

                    environment{
                        JMETER_HOME="/u01/app/jmeter/apache-jmeter-5.1.1";
                        JMX_FILE_LOC="${APP_ROOT_DIR}/jmeter_test_cases/buyer.jmx"
                        JMX_RESULT_FILE_LOC="${APP_ROOT_DIR}/target/result.file"
                        JMX_WEB_REP_LOC="${APP_ROOT_DIR}/target/jmxreport"
                    }

                    steps {
                        echo "Performance test is in progress ...."
                        sh script: ''' [ -d ${JMX_WEB_REP_LOC} ] &&  rm -rf  ${JMX_WEB_REP_LOC}
                            [ -f ${JMX_RESULT_FILE_LOC} ] &&  rm -rf  ${JMX_RESULT_FILE_LOC}
                            mkdir -p ${JMX_WEB_REP_LOC}
                            ${JMETER_HOME}/bin/jmeter.sh -n -t ${JMX_FILE_LOC} -l ${JMX_RESULT_FILE_LOC} -e -o ${JMX_WEB_REP_LOC}
                            '''
                    }
                    post {
                        success {
                            perfReport sourceDataFiles: env.JMX_RESULT_FILE_LOC
                        }
                    }
                }
                
                stage('Functional Regression Test') {
                    steps {
                        echo "Function test is in progress...."
                        sh 'mvn -f ${APP_ROOT_DIR}/pom.xml -Drevision="${REVISION_ID}" failsafe:integration-test'
                    }
                    post {
                        always {
                            publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '${APP_ROOT_DIR}/target/failsafe-reports', reportFiles: 'index.html', reportName: 'Integration Test Report', reportTitles: 'IT Test Result'])
                        }
                    }
                }
                */
            }
        }

        stage('Checklist report generation'){
            steps {
                echo "Generating checklist report"
            }
        }
        
    }
}
