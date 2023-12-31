REPOSITORY=/home/ubuntu/app
cd $REPOSITORY

APP_NAME=I_MU
JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'I_MU-0.0.1-SNAPSHOT.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

CURRENT_PID=$(pgrep -f $APP_NAME)

if [ -z $CURRENT_PID ]
then
  echo "> 현재 구동중인 애플리케이션이 존재하지 않습니다."
else
  echo "> kill -9 $CURRENT_PID"
  sudo kill -9 $CURRENT_PID
  sleep 5
fi

echo "> $JAR_PATH 배포"
nohup java -jar \
        build/libs/$JAR_NAME > $REPOSITORY/nohup.out 2>&1 &
