# Installing SleuthKit



## UNIX
##Installing tools
```console
sudo apt-get install autoreconf
autoreconf --install
sudo apt-get install autoconf
sudo apt-get install automake
sudo apt-get install autopoint
sudo apt-get install libtoolize
sudo apt-get install pkg-config
sudo apt-get install flex
sudo apt-get install byacc
sudo apt-get install aclocal
pip3 install --user meson
pip3 install --user pytest
```

##Dependent projects

#FUSE
```console
https://github.com/libfuse/libfuse
cd libfuse
mkdir build
cd build
~/.local/bin/meson ..
ninja
sudo python3 -m pytest test/
```

#AFFLIBv3  3.3.6
```console
git clone https://github.com/sshock/AFFLIBv3
cd AFFLIBv3/
git checkout tags/v3.7.16 #3.6 juz nie ma
./bootstrap
sudo ./configure
sudo make all install
```

#LIBEWF
```console
https://github.com/libyal/libewf
cd libewf/
./synclibs.sh
./autogen.sh
./configure
sudo make
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
sudo ./configure
sudo make
```

### Building JAR

```console
cd bindings/java/
ant
```
If ant building ends without errors, jar will be in /dist directory. 
