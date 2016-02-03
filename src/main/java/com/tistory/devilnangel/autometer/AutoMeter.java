package com.tistory.devilnangel.autometer;

import com.tistory.devilnangel.autometer.common.HTTPResultCalculator;
import com.tistory.devilnangel.autometer.reporters.AutoMeterResultCollector;
import com.tistory.devilnangel.autometer.reporters.SystemInfoCollector;
import com.tistory.devilnangel.autometer.savers.CVSFileSaver;
import com.tistory.devilnangel.autometer.util.PropertiesUtil;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SummaryReport;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <h2>One Thread Group & HttpRequest & local JMeter Tester</h2>
 *
 * <h3>usage</h3>
 * <ol>
 *     <li>set env "AUTOMETER_HOME" or set property autometer.home</li>
 *     <li>{@link #setNumOfThread(int)}</li>
 *     <li>{@link #setRampUpTime(int)}</li>
 *     <li>{@link #setLoopCount(int)}</li>
 *     <li>{@link #addHttpSampler(HTTPSamplerProxy)} or {@link #addHttpSampler(String, int, String, String)}</li>
 *     <li>{@link #doTest()}</li>
 * </ol>
 *
 * @author k, Created on 16. 1. 26.
 */
@Slf4j
public @Data class AutoMeter {

    private final static String DEFAULT_TEST_PLAN_NAME = "test plan form java code";
    private final static String DEFAULT_THREAD_GROUP_NAME = "default thread group";
    private final static String DEFAULT_LOOP_CONTROLLER_NAME = "default loop controller";
    private final static String DEFAULT_RESULT_COLLECTOR_NAME = "default result collector";

    private JMeterEngine jmeter;
    private final List<Sampler> sampler = new ArrayList<Sampler>();
    private final HashTree testPlanTree = new HashTree();
    private LoopController loopController;
    private ThreadGroup threadGroup;
    private final TestPlan testPlan = new TestPlan();
    private boolean loopForever;
    private Arguments userDefinedArguments;
    private AutoMeterResultCollector resultCollector;
    private final HTTPResultCalculator calculator = new HTTPResultCalculator();
    private final ResultViewer resultViewer = new ResultViewer();

    /**
     * flag for collect system information
     */
    private boolean collectRemoteSystemInfo;
    /**
     * if system information collect is true, this list will be populated
     * with domain name and system information collector which {@link SystemInfoCollector} instance
     */
    private final HashMap<String, SystemInfoCollector> httpDomains = new HashMap<String, SystemInfoCollector>();

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
    private int loopCount  = 1;

    /**
     * JMeter initialization (properties, log levels, locale, etc)
     */
    private void initJMeter() {

        JMeterUtils.setJMeterHome(PropertiesUtil.getBaseDir());
        JMeterUtils.loadJMeterProperties(PropertiesUtil.getJMeterPropertiesFile());
        JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
        JMeterUtils.initLocale();
        userDefinedArguments = (Arguments) new ArgumentsPanel().createTestElement();
    }

    /**
     * Loop Controller
     */
    private void initDefaultLoopController() {
        loopController = new LoopController();
        loopController.initialize();
        loopController.setLoops(loopCount);
        loopController.setFirst(true);
        loopController.setName(DEFAULT_LOOP_CONTROLLER_NAME);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
    }

    public void setDirs(@NonNull String baseDir) throws IOException { PropertiesUtil.setDirs(baseDir); }
    /**
     * Thread Group
     */
    private void initDefaultThreadGroup() {

        if (threadGroup == null) {
            threadGroup = new ThreadGroup();
            threadGroup.setName(DEFAULT_THREAD_GROUP_NAME);
            threadGroup.setNumThreads(numOfThread);
            threadGroup.setRampUp(rampUpTime);

            if (loopController == null) initDefaultLoopController();
            threadGroup.setSamplerController(loopController);
            loopController.setContinueForever(loopForever); // must be after setSamplerController

            threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
            threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        }
    }

    private void initTestPlan() {
        if (testPlan.getName() == null) {
            testPlan.setName(DEFAULT_TEST_PLAN_NAME);
        }
        testPlan.setUserDefinedVariables(userDefinedArguments);
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
    }

    /**
     * add {@link HTTPSampler}
     * @param sampler
     */
    public void addHttpSampler(@NonNull HTTPSamplerProxy sampler) {
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        sampler.setUseKeepAlive(true);
        sampler.setAutoRedirects(true);
        this.sampler.add(sampler);
        String domain = sampler.getDomain();
        if (domain !=null && !this.httpDomains.containsKey(domain)) {
            this.httpDomains.put(sampler.getDomain(), new SystemInfoCollector(domain));
        }
    }

    /**
     * add {@link HTTPSampler}
     * @param domain service domain
     * @param port service port
     * @param path content path
     * @param method http request method
     */
    public void addHttpSampler(String domain, int port, String path, String method) {
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setDomain(domain);
        sampler.setPort(port);
        sampler.setPath(path);
        sampler.setMethod(method);
        addHttpSampler(sampler);
    }

    public void setTestPlanName(String name) { testPlan.setName(name); }
    /**
     * Construct Test Plan from previously initialized elements
     *
     * <ol>
     *     <li>prepare test</li>
     *     <li>init jmeter</li>
     *     <li>init test plan</li>
     *     <li>init ThreadGroup - init LoopController</li>
     *     <li>init ResultCollector</li>
     *     <li>init SystemInfoCollector</li>
     *     <li>add user defined arguments</li>
     * </ol>
     *
     * {@link #addHttpSampler(HTTPSamplerProxy)} or {@link #addHttpSampler(String, int, String, String)} should be called before this method called
     */
    void constructTestPlan() {

        //JMeter Engine
        setJmeter(new StandardJMeterEngine());

        initJMeter();

        initTestPlan();

        // Thread Group
        initDefaultThreadGroup();

        initDefaultResultCollector();

        initSystemInfoCollector();

        // add test plan
        testPlanTree.add(testPlan);

        // add threadGroup under test plan
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(sampler);

        testPlanTree.add(testPlanTree.getArray()[0], resultCollector);
    }

    private void initDefaultResultCollector() {
        //add Summarizer output to get test progress in stdout like:
        // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
        Summariser summer=null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }
        // Store execution results into a .jtl file
        //String logFile = "example.jtl";
        resultCollector = new AutoMeterResultCollector(summer);
        resultCollector.setName(DEFAULT_RESULT_COLLECTOR_NAME);
        resultCollector.setProperty(TestElement.TEST_CLASS, AutoMeterResultCollector.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS, SummaryReport.class.getName());
        resultCollector.setCalculator(calculator);
        calculator.setResultViewer(resultViewer);
        //resultCollector.setFilename(logFile);
    }

    private void initSystemInfoCollector() {
        if (collectRemoteSystemInfo) {
            ShutdownHandler sh = new ShutdownHandler();
            for (String domain : httpDomains.keySet()) {
                sh.addThread(httpDomains.get(domain).start());
            }
            resultCollector.setHttpDomains(httpDomains);

            Runtime.getRuntime().addShutdownHook(new Thread(sh));
        }
    }

    public void addArgument(String name, String value) {
        userDefinedArguments.addArgument(new Argument(name, value, (String)null));
    }

    public void addArgument(Argument arg) {
        userDefinedArguments.addArgument(arg);
    }

    public void addArgument(String name, String value, String metadata) {
        userDefinedArguments.addArgument(new Argument(name, value, metadata));
    }

    /**
     * print test plan to stdout
     *
     * @throws IOException
     */
    public final void printTestPlanJmx() throws IOException {
        constructTestPlan();

        SaveService.saveTree(testPlanTree, System.out);

    }

    /**
     * <ol>
     *     <li>construct TestPlan</li>
     *     <li>run test</li>
     * </ol>
     */
    public final void doTest() {

        constructTestPlan();
        calculator.start();
        resultViewer.start();

        // Run Test Plan
        getJmeter().configure(testPlanTree);
        ((StandardJMeterEngine)jmeter).run();
    }

    public void setCSVSaveFile(String filename) throws FileNotFoundException, UnsupportedEncodingException {
        resultViewer.setSaver(new CVSFileSaver(filename));
    }

    private static final CmdOptions options = new CmdOptions();

    public static void main(String[] args) throws ParseException {

        options.setArgs(args);
        CommandLine cmd = options.getCmd();

        if (cmd.hasOption("h")) {
            options.printUsage();
            return;
        }

        if (cmd.hasOption("d")) {

            try {
                PropertiesUtil.setDirs(cmd.getOptionValue("d").trim());

            } catch (IOException e) {

                System.err.println(PropertiesUtil.getConfDir() + File.separator + PropertiesUtil.AppProperties + " not found. may use -d option" + e);
            }
        }

        AutoMeter autoMeter = new AutoMeter();
        autoMeter.doTest();

    }


    private final class ShutdownHandler implements Runnable {

        private final ArrayList<Thread> threads = new ArrayList<Thread>();
        public void addThread(Thread t) {
            threads.add(t);
        }
        @Override
        public void run() {
            for (Thread t : threads) {
                t.interrupt();
                try {
                    t.join(1000);
                } catch (InterruptedException e) {
                    log.error("shutdown "+t.toString()+" interrupted ", e);
                }
            }
        }
    }

}
