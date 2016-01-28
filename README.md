# autometer
automated jmeter with java code

## installed directory structure

    AUTOMETER_HOME
        +bin/
        +conf/

## usage

1. set env "AUTOMETER_HOME" or set property autometer.home

2. setNumOfThread(int)

3. setRampUpTime(int)

4. if want loop setLoopCont(int), if loop forever setLoopForever(true)

4. addHttpSampler(HTTPSampler) or addHttpSampler(String, int, String, String)

5. doTest()