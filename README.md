# Spring Boot

## 创建项目

```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.4.0</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <groupId>org.example</groupId>
    <artifactId>jib-docker</artifactId>
    <version>V2</version>
    <name>jib</name>
    <description>Demo project for Spring Boot docker</description>


    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
```

**添加方法**

```java
@RestController
public class IndexController {

    @RequestMapping("/")
    public String hello(){
        return "Hello, " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
```

添加启动类

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 方式一：Dockerfile

1. 创建Dockerfile文件

   ```
   FROM java:8
   COPY *.jar /app.jar
   
   CMD ["--server.port=8080"]
   
   EXPOSE 8080
   
   ENTRYPOINT ["java","-jar","/app.jar"]
   ```

2. 打包项目 `mvn package`

3. 上传jar包和Dockerfile 文件 `scp xxx.jar Dockerfile` 请在同一目录下jar 包和Dockerfile

4. 构建镜像：`docker build -t springboot-demo .`

   ![](https://java-note-pic.oss-cn-beijing.aliyuncs.com/java/202207060828665.png)

5. 运行镜像：`docker run -d -p 39005:8080 --name my-springboot springboot-demo`

6. 开启端口：`firewall-cmd --zone=public --add-port=8080/tcp --permanent` 重启：`systemctl restart firewalld`

7. 浏览器测试 http://ip:8080



## 方式二：dockerfile-maven

官方：https://github.com/spotify/dockerfile-maven



**主要特点**

1. 构建基于Dockerfile文件，所以需要一个 Dockerfile 文件
2. 把 Docker 的构建过程集成到了 Maven 的构建过程之中，合并了打包和推送命令
3. 与 Maven 构建集成，可以在一个项目中依赖另外一个项目的 Docker 镜像，正确的顺序构建项目

**实现**

1. 创建POM文件

   ```xml
   <build>
       <plugins>
           <plugin>
               <groupId>com.spotify</groupId>
               <artifactId>dockerfile-maven-plugin</artifactId>
               <version>1.4.13</version>
               <executions>
                   <execution>
                       <id>default</id>
                       <goals>
                           <goal>build</goal>
                           <goal>push</goal>
                       </goals>
                   </execution>
               </executions>
               <configuration>
                   <repository>javastack/${project.name}</repository>
                   <tag>${project.version}</tag>
                   <buildArgs>
                       <JAR_FILE>${project.build.finalName}.jar</JAR_FILE>
                   </buildArgs>
                   <dockerfile>src/main/docker/Dockerfile</dockerfile>
               </configuration>
           </plugin>
       </plugins>
   </build>
   ```

2. 创建Dockerfile

   ```dockerfile
   FROM java:8
   
   ARG JAR_FILE
   
   ADD target/${JAR_FILE} app.jar
   
   ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
   ```

3. 开启防火墙

4. 项目测试





## 方式三：jib

编写POM文件

```xml
<build>
        <finalName>jib-docker</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>com.index.Application</mainClass>
                </configuration>
            </plugin>
            <plugin>
                <groupId>io.fabric8</groupId>
                <artifactId>docker-maven-plugin</artifactId>
                <version>0.33.0</version>
                <configuration>
                    <dockerHost>http://192.168.232.130:2375</dockerHost>
                    <!-- Docker 推送镜像仓库地址-->
                    <!--  <pushRegistry>http://192.168.3.101:5000</pushRegistry>-->
                    <images>
                        <image>
                            <name>192.168.3.101:5000/${project.name}:${project.version}</name>
                            <build>
                                <from>java:8</from>
                                <args>
                                    <JAR_FILE>${project.build.finalName}.jar</JAR_FILE>
                                </args>
                                <assembly>
                                    <targetDir>/</targetDir>
                                    <descriptorRef>artifact</descriptorRef>
                                </assembly>
                                <entryPoint>["java", "-jar","/${project.build.finalName}.jar"]</entryPoint>
                                <maintainer>yan</maintainer>
                            </build>
                        </image>
                    </images>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

执行`` mvn clean package docker:build`

![](https://java-note-pic.oss-cn-beijing.aliyuncs.com/java/202207050848315.png)

运行容器：`docker run -it -p 8080:8080 89620b24146a jib-docker`

![](https://java-note-pic.oss-cn-beijing.aliyuncs.com/java/202207050849048.png)

浏览器运行：``http:192.168.232.130:8080`

![](https://java-note-pic.oss-cn-beijing.aliyuncs.com/java/202207050850745.png)



方法二：自己编写Dockerfile 文件 然后引入

```dockerfile
FROM java:8
COPY maven /
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/mall-tiny-fabric-0.0.1-SNAPSHOT.jar"]
MAINTAINER macrozheng
```

插件引入

```xml
<build>
     <dockerFileDir>${project.basedir}</dockerFileDir>
</build>
```



## 备注

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.spotify</groupId>
            <artifactId>docker-maven-plugin</artifactId>
            <version>1.2.2</version>
            <configuration>
                <imageName>${docker.image.prefix}/${project.artifactId}</imageName>
                <dockerDirectory>src/main/docker</dockerDirectory>
                <resources>
                    <resource>
                        <targetPath>/</targetPath>
                        <directory>${project.build.directory}</directory>
                        <include>${project.build.finalName}.jar</include>
                    </resource>
                </resources>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**官方已不维护**，而是使用dockerfile-maven
