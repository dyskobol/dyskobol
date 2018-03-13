# Installing SleuthKit
## UNIX
##Installing tools
```console
sudo apt-get install autoreconf
autoreconf --install
```

### Downloading and extracting
```console
wget https://github.com/sleuthkit/sleuthkit/releases/download/sleuthkit-4.6.0/sleuthkit-4.6.0.tar.gz
unzip sleuthkit-4.6.0.tar.gz
```

### Building C library

```console
cd sleuthkit-sleuthkit-4.6.0
autoconf configure.ac
chmod u+x ./configure
./configure
make
```

### Building JAR

```console
cd bindings/java/
ant
```
If ant building ends without errors, jar will be in /dist directory. 
