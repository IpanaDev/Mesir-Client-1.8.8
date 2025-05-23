package net.minecraft.crash;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.minecraft.util.ReportedException;
import net.minecraft.world.gen.layer.IntCache;
import optifine.CrashReportCpu;
import optifine.CrashReporter;
import optifine.Reflector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrashReport
{
    private static final Logger logger = LogManager.getLogger();

    /** Description of the crash report. */
    private final String description;

    /** The Throwable that is the "cause" for this crash and Crash Report. */
    private final Throwable cause;

    /** Category of crash */
    private final CrashReportCategory theReportCategory = new CrashReportCategory(this, "System Details");

    /** Holds the keys and values of all crash report sections. */
    private final List<CrashReportCategory> crashReportSections = Lists.newArrayList();

    /** File of crash report. */
    private File crashReportFile;
    private boolean field_85059_f = true;
    private StackTraceElement[] stacktrace = new StackTraceElement[0];
    private boolean reported = false;

    public CrashReport(String descriptionIn, Throwable causeThrowable)
    {
        this.description = descriptionIn;
        this.cause = causeThrowable;
        this.populateEnvironment();
    }

    /**
     * Populates this crash report with initial information about the running server and operating system / java
     * environment
     */
    private void populateEnvironment()
    {
        this.theReportCategory.addCrashSectionCallable("Minecraft Version", () -> "1.8.8");
        this.theReportCategory.addCrashSectionCallable("Operating System", () -> System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
        this.theReportCategory.addCrashSectionCallable("CPU", () -> String.valueOf(new CrashReportCpu()));
        this.theReportCategory.addCrashSectionCallable("Java Version", () -> System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        this.theReportCategory.addCrashSectionCallable("Java VM Version",  () -> System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
        this.theReportCategory.addCrashSectionCallable("Memory",  () -> {
            Runtime runtime = Runtime.getRuntime();
            long i = runtime.maxMemory();
            long j = runtime.totalMemory();
            long k = runtime.freeMemory();
            long l = i / 1024L / 1024L;
            long i1 = j / 1024L / 1024L;
            long j1 = k / 1024L / 1024L;
            return k + " bytes (" + j1 + " MB) / " + j + " bytes (" + i1 + " MB) up to " + i + " bytes (" + l + " MB)";
        });
        this.theReportCategory.addCrashSectionCallable("JVM Flags", () -> {
            RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
            List list = runtimemxbean.getInputArguments();
            int i = 0;
            StringBuilder stringbuilder = new StringBuilder();

            for (Object s : list)
            {
                if (((String) s).startsWith("-X"))
                {
                    if (i++ > 0)
                    {
                        stringbuilder.append(" ");
                    }

                    stringbuilder.append(s);
                }
            }

            return String.format("%d total; %s", i, stringbuilder.toString());
        });
        this.theReportCategory.addCrashSectionCallable("IntCache", IntCache::getCacheSizes);

    }

    /**
     * Returns the description of the Crash Report.
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the Throwable object that is the cause for the crash and Crash Report.
     */
    public Throwable getCrashCause()
    {
        return this.cause;
    }

    /**
     * Gets the various sections of the crash report into the given StringBuilder
     */
    public void getSectionsInStringBuilder(StringBuilder builder)
    {
        if ((this.stacktrace == null || this.stacktrace.length <= 0) && this.crashReportSections.size() > 0)
        {
            this.stacktrace = ArrayUtils.subarray((this.crashReportSections.get(0)).getStackTrace(), 0, 1);
        }

        if (this.stacktrace != null && this.stacktrace.length > 0)
        {
            builder.append("-- Head --\n");
            builder.append("Stacktrace:\n");

            for (StackTraceElement stacktraceelement : this.stacktrace)
            {
                builder.append("\t").append("at ").append(stacktraceelement.toString());
                builder.append("\n");
            }

            builder.append("\n");
        }

        for (Object crashreportcategory : this.crashReportSections)
        {
            ((CrashReportCategory) crashreportcategory).appendToStringBuilder(builder);
            builder.append("\n\n");
        }

        this.theReportCategory.appendToStringBuilder(builder);
    }

    /**
     * Gets the stack trace of the Throwable that caused this crash report, or if that fails, the cause .toString().
     */
    public String getCauseStackTraceOrString()
    {
        StringWriter stringwriter = null;
        PrintWriter printwriter = null;
        Throwable object = this.cause;

        if (object.getMessage() == null)
        {
            if (object instanceof NullPointerException)
            {
                object = new NullPointerException(this.description);
            }
            else if (object instanceof StackOverflowError)
            {
                object = new StackOverflowError(this.description);
            }
            else if (object instanceof OutOfMemoryError)
            {
                object = new OutOfMemoryError(this.description);
            }

            object.setStackTrace(this.cause.getStackTrace());
        }

        String s;

        try
        {
            stringwriter = new StringWriter();
            printwriter = new PrintWriter(stringwriter);
            object.printStackTrace(printwriter);
            s = stringwriter.toString();
        }
        finally
        {
            IOUtils.closeQuietly(stringwriter);
            IOUtils.closeQuietly(printwriter);
        }

        return s;
    }

    /**
     * Gets the complete report with headers, stack trace, and different sections as a string.
     */
    public String getCompleteReport()
    {
        if (!this.reported)
        {
            this.reported = true;
            CrashReporter.onCrashReport(this, this.theReportCategory);
        }

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("---- Minecraft Crash Report ----\n");
        stringbuilder.append("// ");
        stringbuilder.append(getWittyComment());
        stringbuilder.append("\n\n");
        stringbuilder.append("Time: ");
        stringbuilder.append((new SimpleDateFormat()).format(new Date()));
        stringbuilder.append("\n");
        stringbuilder.append("Description: ");
        stringbuilder.append(this.description);
        stringbuilder.append("\n\n");
        stringbuilder.append(this.getCauseStackTraceOrString());
        stringbuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        for (int i = 0; i < 87; ++i)
        {
            stringbuilder.append("-");
        }

        stringbuilder.append("\n\n");
        this.getSectionsInStringBuilder(stringbuilder);
        return stringbuilder.toString();
    }

    /**
     * Gets the file this crash report is saved into.
     */
    public File getFile()
    {
        return this.crashReportFile;
    }

    /**
     * Saves this CrashReport to the given file and returns a value indicating whether we were successful at doing so.
     */
    public boolean saveToFile(File toFile)
    {
        if (this.crashReportFile != null)
        {
            return false;
        }
        else
        {
            if (toFile.getParentFile() != null)
            {
                toFile.getParentFile().mkdirs();
            }

            try
            {
                FileWriter filewriter = new FileWriter(toFile);
                filewriter.write(this.getCompleteReport());
                filewriter.close();
                this.crashReportFile = toFile;
                return true;
            }
            catch (Throwable throwable)
            {
                logger.error("Could not save crash report to " + toFile, throwable);
                return false;
            }
        }
    }

    public CrashReportCategory getCategory()
    {
        return this.theReportCategory;
    }

    /**
     * Creates a CrashReportCategory
     */
    public CrashReportCategory makeCategory(String name)
    {
        return this.makeCategoryDepth(name, 1);
    }

    /**
     * Creates a CrashReportCategory for the given stack trace depth
     */
    public CrashReportCategory makeCategoryDepth(String categoryName, int stacktraceLength)
    {
        CrashReportCategory crashreportcategory = new CrashReportCategory(this, categoryName);

        if (this.field_85059_f)
        {
            int i = crashreportcategory.getPrunedStackTrace(stacktraceLength);
            StackTraceElement[] astacktraceelement = this.cause.getStackTrace();
            StackTraceElement stacktraceelement = null;
            StackTraceElement stacktraceelement1 = null;
            int j = astacktraceelement.length - i;

            if (j < 0)
            {
                System.out.println("Negative index in crash report handler (" + astacktraceelement.length + "/" + i + ")");
            }

            if (0 <= j && j < astacktraceelement.length)
            {
                stacktraceelement = astacktraceelement[j];

                if (astacktraceelement.length + 1 - i < astacktraceelement.length)
                {
                    stacktraceelement1 = astacktraceelement[astacktraceelement.length + 1 - i];
                }
            }

            this.field_85059_f = crashreportcategory.firstTwoElementsOfStackTraceMatch(stacktraceelement, stacktraceelement1);

            if (i > 0 && !this.crashReportSections.isEmpty())
            {
                CrashReportCategory crashreportcategory1 = this.crashReportSections.get(this.crashReportSections.size() - 1);
                crashreportcategory1.trimStackTraceEntriesFromBottom(i);
            }
            else if (astacktraceelement.length >= i && 0 <= j && j < astacktraceelement.length)
            {
                this.stacktrace = new StackTraceElement[j];
                System.arraycopy(astacktraceelement, 0, this.stacktrace, 0, this.stacktrace.length);
            }
            else
            {
                this.field_85059_f = false;
            }
        }

        this.crashReportSections.add(crashreportcategory);
        return crashreportcategory;
    }

    /**
     * Gets a random witty comment for inclusion in this CrashReport
     */
    private static String getWittyComment()
    {
        String[] astring = new String[] {
                "Başaramadık Abi",
                "Oh bugünde IP vermedik koy göte",
                "Eriq VS KORWEN",
                "SemoTeoFanboy",
                "Sıçtık Cafer peçete getir",
                "ÖSS yi kaçırmışsın",
                "Ismail 6 yıl universite okumuş",
                "Ilk tick sonra hurttime vuran aura",
                "ABİ EKRANIM TERS GELDI",
                "Crow vs Karga",
                "Ready Client Developer",
                "Allahsiz Buğra",
                "Eğeğeğeğğe.",
                "KıymaCraft",
                "SeriexGQ SeriexNW SeriexCraft",
                "Google a anancraft yaz bak ne çıkıyo",
                "Crash yedik çok riskli oldu",
                "Kafanı kaşıma tarak ister arkası crash ister",
                "ARIS DEN CRASH REPORT DILENMIŞSIN",
                "Streax Client",
                "Ruby çakması",
                "Ipana Reborn bunu keser",
                "MehmetAga502 mi keser IpanaDev mi",
                "kixxa abimi sinirlendirmeyin",
                "kixxa crash attı",
                "bok gibi hile olm daha telefonda açılmıyo",
                "OBF Delinmiştir",
                "RATSIZ!1!!!1",
                "HurtTayyip",
                "bok ye za",
                "Erik her ay sonu medinede görülmüştür.",
                "CAMERA.class",
                "FIGHT_SPEED Client"};

        try
        {
            return astring[(int)(System.nanoTime() % (long)astring.length)];
        }
        catch (Throwable var2)
        {
            return "Witty comment unavailable :(";
        }
    }

    /**
     * Creates a crash report for the exception
     */
    public static CrashReport makeCrashReport(Throwable causeIn, String descriptionIn)
    {
        CrashReport crashreport;

        if (causeIn instanceof ReportedException)
        {
            crashreport = ((ReportedException)causeIn).getCrashReport();
        }
        else
        {
            crashreport = new CrashReport(descriptionIn, causeIn);
        }

        return crashreport;
    }
}
