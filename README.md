minifyhtml
==========

一个maven plugin，用来自动压缩项目文件中html文件，可以与yuicompressor配合使用。
该plugin也是利用yui compressor来对html中的js和css进行压缩的。

典型配置：

``` xml
<plugin>
	<groupId>net.alchim31.maven</groupId>
	<artifactId>yuicompressor-maven-plugin</artifactId>
	<executions>
		<execution>
			<id>compressyui</id>
			<phase>process-resources</phase>
			<goals>
				<goal>compress</goal>
			</goals>
		</execution>
	</executions>
	<configuration>
		<nosuffix>true</nosuffix>
		<encoding>${project.build.sourceEncoding}</encoding>
		<webappDirectory>${project.build.directory}/WebRoot</webappDirectory>
		<warSourceDirectory>${basedir}/WebRoot</warSourceDirectory>
		<includes>
              <include>**/*.js</include>
              <include>**/*.css</include>
       </includes>
		<excludes>
			<exclude>**/*.min.js</exclude>
			<exclude>src</exclude>
			<exclude>**/books</exclude>
		</excludes>
	</configuration>
</plugin>
<plugin>
	<groupId>com.puma.minifyhtml</groupId>
	<artifactId>minifyhtml-maven-plugin</artifactId>
	<version>1.0.0</version>
	<executions>
		<execution>
			<id>minifyhtml</id>
			<phase>process-sources</phase>
			<goals>
				<goal>compress</goal>
			</goals>
		</execution>
	</executions>
	<configuration>
		<encoding>${project.build.sourceEncoding}</encoding>
		<webappDirectory>${project.build.directory}/WebRoot</webappDirectory>
		<warSourceDirectory>${basedir}/WebRoot</warSourceDirectory>
	</configuration>
</plugin>
``` 
