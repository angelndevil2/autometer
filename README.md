[![Build Status](https://travis-ci.org/angelndevil2/autometer.svg?branch=master)](https://travis-ci.org/angelndevil2/autometer)

# autometer
automated jmeter with java code

## installed directory structure

    autometer home
        +bin/
        +conf/

## usage

1. setDirs(String) : set autometer home

2. setNumOfThread(int)

3. setRampUpTime(int)

4. if want loop setLoopCont(int), if loop forever setLoopForever(true)

4. addHttpSampler(HTTPSampler) or addHttpSampler(String, int, String, String)

5. doTest()

* cpu busy% collect if setCollectRemoteSystemInfo(true) and [system-info](https://github.com/angelndevil2/system-info) package is installed in target machine
