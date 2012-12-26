/**
 * (c) Zoltan Farkas 2012
 */
package com.zoltran.stackmonitor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zoly
 */
public class Monitor {

    private static final Logger log = LoggerFactory.getLogger(Monitor.class);
    
    private static class Options {

        @Option(name = "-f", usage = "output to this file the perf report, format is HTML")
        private String reportOut;
        @Option(name = "-main", usage = "the main class name", required = true)
        private String mainClass;
        @Option(name = "-si", usage = "the stack sampling interval in milliseconds")
        private int sampleInterval = 100;
        @Option(name = "-w", usage = "flame chart width in pixels")
        private int chartWidth = 2000;
        @Option(name = "-md", usage = "maximum stack trace depth")
        private int maxDepth = Integer.MAX_VALUE;
        @Option(name = "-ss", usage = "start the stack sampling thread. (can also be done manually via jmx)")
        private boolean startSampler = false;
        @Option(name = "-nosvg", usage = "stack visualization will be in svg format")
        private boolean noSvgReport = false;
        
    }
    private static volatile boolean generatedAndDisposed;

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, 
            MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, 
            NotCompliantMBeanException, InterruptedException {

        generatedAndDisposed = false;
        
        int sepPos = args.length;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--")) {
                sepPos = i;
                break;
            }
        }
        String[] newArgs;
        String[] ownArgs;
        if (sepPos == args.length) {
            newArgs = new String[0];
            ownArgs = args;
        } else {
            newArgs = new String[args.length - sepPos - 1];
            ownArgs = new String[sepPos];
            System.arraycopy(args, sepPos + 1, newArgs, 0, newArgs.length);
            System.arraycopy(args, 0, ownArgs, 0, sepPos);
        }
        Options options = new Options();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(ownArgs);
        } catch (CmdLineException e) {
            System.err.println("Error: " + e.getMessage() + "\nUsage:");
            parser.printUsage(System.err);
            System.exit(1);
        }
        final String reportOut = options.reportOut;
        final int chartWidth = options.chartWidth;
        final int maxDepth = options.maxDepth;
        final boolean svgReport = !options.noSvgReport;

        final Sampler sampler = new Sampler(options.sampleInterval, new SimpleStackCollector());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    sampler.stop();
                    generateReportAndDispose(sampler, reportOut, chartWidth, maxDepth, svgReport);
                } catch (Exception ex) {
                    log.error("Exception while shutting down", ex);
                }
            }

        }, "Sampling report"));
        sampler.registerJmx();

        if (options.startSampler) {
            sampler.start();
        }
        Class.forName(options.mainClass).getMethod("main", String[].class).invoke(null, (Object) newArgs);
    }
    
    private static void generateReportAndDispose(final Sampler sampler,
            final String reportOut, final int chartWidth, final int maxDepth, boolean svgReport) throws IOException, InterruptedException {
                synchronized (Monitor.class) {
                    if (!generatedAndDisposed) {
                        if (svgReport) {
                            sampler.generateSvgHtmlMonitorReport(reportOut, chartWidth, maxDepth);                         
                        }
                        else {
                            sampler.generateHtmlMonitorReport(reportOut, chartWidth, maxDepth);
                        }
                        log.info("Sample report written to {}", reportOut);
                        sampler.dispose();
                        generatedAndDisposed = true;
                    }
                }
    }
    
}
