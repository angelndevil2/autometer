package kr.blogspot.andmemories;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>One Thread Group & HttpRequest & local JMeter Tester</h2>
 *
 * <h3>usage</h3>
 * <ol>
 *     <li>set env "AUTOMETER_HOME" or set property autometer.home</li>
 *     <li>{@link #setNumOfThread(int)}</li>
 *     <li>{@link #setRampUpTime(int)}</li>
 *     <li>{@link #addHttpSampler(HTTPSampler)} or {@link #addHttpSampler(String, int, String, String)}</li>
 *     <li>{@link #doTest()}</li>
 * </ol>
 *
 * @author k, Created on 16. 1. 26.
 */
public @Data class AutoMeter {

    private JMeterEngine jmeter;
    private final List<Sampler> sampler = new ArrayList<Sampler>();
    @Setter(AccessLevel.NONE)
    private String baseDir;
    @Setter(AccessLevel.NONE)
    private String confDir;
    @Setter(AccessLevel.NONE)
    private String binDir;
    private HashTree testPlanTree;
    private LoopController loopController;
    private ThreadGroup threadGroup;
    private TestPlan testPlan;
    /**
     * ThreadGroup ramp up time in seconds
     */
    private int rampUpTime = 1;
    /**
     * Thread number in ThreadGroup
     */
    private int numOfThread = 1;
    /**
     * {@link LoopController}'s test loop count
     */
    private int loopCount  =1;
    /**
     * set directory's from env or properties
     */
    private void prepareTest() {
        String baseDir = System.getenv("AUTOMETER_HOME");
        if (baseDir == null) {
            baseDir = System.getProperty("autometer.home");
        }
        if (baseDir == null) baseDir = ".";

        setDirs(baseDir);
    }

    /**
     * JMeter initialization (properties, log levels, locale, etc)
     */
    private void initJMeter() {

        JMeterUtils.setJMeterHome(baseDir);
        JMeterUtils.loadJMeterProperties(confDir+ File.separator +"jmeter.properties");
        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
        JMeterUtils.initLocale();

    }

    /**
     * Loop Controller
     */
    private void initLoopController() {

        if (getLoopController() == null) {
            loopController = new LoopController();
            loopController.setLoops(getLoopCount());
            for (Sampler s : sampler)
                loopController.addTestElement(s);
            loopController.setFirst(true);
            loopController.initialize();
        }
    }

    /**
     * Thread Group
     */
    private void initThreadGroup() {

        if (getThreadGroup() == null) {
            threadGroup = new ThreadGroup();
            threadGroup.setNumThreads(getNumOfThread());
            threadGroup.setRampUp(getRampUpTime());
            threadGroup.setSamplerController(getLoopController());
        }
    }

    /**
     * set application directories
     */
    public void setDirs(@NonNull final String baseDir) {
        this.baseDir = baseDir;
        this.confDir = baseDir + File.separator + "conf";
        this.binDir = baseDir + File.separator + "bin";
    }

    /**
     * add {@link HTTPSampler}
     * @param sampler
     */
    public void addHttpSampler(HTTPSampler sampler) {
        this.sampler.add(sampler);
    }

    /**
     * add {@link HTTPSampler}
     * @param domain service domain
     * @param port service port
     * @param path content path
     * @param method http request method
     */
    public void addHttpSampler(String domain, int port, String path, String method) {
        HTTPSampler sampler = new HTTPSampler();
        sampler.setDomain(domain);
        sampler.setPort(port);
        sampler.setPath(path);
        sampler.setMethod(method);
        addHttpSampler(sampler);
    }

    /**
     * Construct Test Plan from previously initialized elements
     */
    private void contructTestPlan() {
        getTestPlanTree().add("testPlan", getTestPlan());
        getTestPlanTree().add("loopController", getLoopController());
        getTestPlanTree().add("httpSampler", getSampler());
        getTestPlanTree().add("threadGroup", getThreadGroup());
    }

    private void initResultCollector() {
        //add Summarizer output to get test progress in stdout like:
        // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
        Summariser summer=null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }
        // Store execution results into a .jtl file
        //String logFile = "example.jtl";
        ResultCollector result_collector = new ResultCollector(summer);
        //ResultCollector result_collector = new ResultCollector();
        //result_collector.setFilename(logFile);
        getTestPlanTree().add(getTestPlanTree().getArray()[0], result_collector);
        //getTestPlanTree().add("httpSampler", result_collector);
    }

    /**
     * <ol>
     *     <li>prepare test</li>
     *     <li>init jmeter</li>
     *     <li>set TestPlanTree</li>
     *     <li>init LoopController</li>
     *     <li>init ThreadGroup</li>
     *     <li>set TestPlan</li>
     *     <li>contruct TestPlan</li>
     *     <li>init ResultCollector</li>
     *     <li>run test</li>
     * </ol>
     *
     * {@link #addHttpSampler(HTTPSampler)} or {@link #addHttpSampler(String, int, String, String)} should be called before this method called
     */
    public final void doTest() {

        prepareTest();

        //JMeter Engine
        setJmeter(new StandardJMeterEngine());

        initJMeter();

        // JMeter Test Plan, basic all u JOrphan HashTree
        setTestPlanTree(new HashTree());

        // Loop Controller
        initLoopController();

        // Thread Group
        initThreadGroup();

        // Test Plan
        setTestPlan(new TestPlan("Create JMeter Script From Java Code"));

        contructTestPlan();

        initResultCollector();

        System.out.println(getTestPlanTree());
        // Run Test Plan
        getJmeter().configure(getTestPlanTree());
        ((StandardJMeterEngine)getJmeter()).run();
    }

    public static void main(String[] args) {

        AutoMeter autoMeter = new AutoMeter();
        autoMeter.doTest();

    }
}
