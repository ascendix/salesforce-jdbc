time ffmpeg -i SOQL-JDBC-IntelliJ-demo.mp4 -vcodec libx264 \
-vf "pad=width=1720:height=1300:x=0:y=1:color=black" -acodec copy result.mp4
#time ffmpeg -i SOQL-JDBC-IntelliJ-demo.mp4 -c:v libx264 -c:a aac -tag:v  hvc1 SOQL-JDBC-IntelliJ-demo-264.mp4
#time ffmpeg -i SOQL-JDBC-IntelliJ-demo.mp4 -c:v libx265 -c:a aac -tag:v -s 1920x1300  hvc1 SOQL-JDBC-IntelliJ-demo-hevc3.mp4
#time ffmpeg -i SOQL-JDBC-IntelliJ-demo.mp4 -c:v libx265 -crf 28 -c:a aac -b:a 128k -tag:v hvc1 SOQL-JDBC-IntelliJ-demo-hevc3.mp4