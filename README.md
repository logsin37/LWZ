# LWZ

## *a compress/decompress util using LWZ*

## how to use?

you can package this project to a jar named `LwzStarter.jar`, and use `java -jar LwzStarter.jar h` to get help info  
more infomation will display in `org.dgk.util.compress.lwz.LwzStarter.java`  
> **note**
> main class declare must write in `LwzStarter.jar/META-INF/MANIFEST.MF`, for example `Main-Class: org.dgk.util.compress.lwz.LwzStarter`. if not, some exception will appear when you run the jar file, and you should manual write this info into your jar file.