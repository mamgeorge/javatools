# Intent

The purpose of this package is to allow various boilerplates and samples, including:
* AWS access
* SQLite, MongoDB
* Encryption
* Json Object transformations
* Swing samples
* Mock samples

https://dzone.com/articles/build-a-java-app-with-gradle

cd C:\workspace\training\javatools

set "build.gradle" application { mainClass.set( 'utils.Aws1Class' ) } 
	although, test class may execute no matter what?

gradle clean build test --i | findstr /i INFO:
gradle runany --info -x test // runs a task
gradle run
