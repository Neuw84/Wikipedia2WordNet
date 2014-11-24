package edu.ehu.galan.wiki2wordnet.wikipedia2wordnet.utils;

/**
 * This class can be used to execute a system command from a Java application.
 * See the documentation for the public methods of this class for more
 * information.
 *
 * Does not support the Sudo command yet
 *
 * Documentation for this class is available at this URL:
 *
 * http://devdaily.com/java/java-processbuilder-process-system-exec
 *
 * Copyright 2010 alvin j. alexander, devdaily.com.
 *
 * Some modifications to allow the change of the working directory of the
 * process and to allow the change of environment variables by Angel Conde, also
 * a method for timing out the process has been added
 *
 * Copyright 2013 neuw84 at gmail dot com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Please ee the following page for the LGPL license:
 * http://www.gnu.org/licenses/lgpl.txt
 *
 *
 */
import java.io.*;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCommandExecutor {

    private final static Logger logger = LoggerFactory.getLogger(SystemCommandExecutor.class);
    private List<String> commandInformation;
    private String adminPassword;
    private ThreadedStreamHandler inputStreamHandler;
    private ThreadedStreamHandler errorStreamHandler;
    private ProcessBuilder pb = null;

    /**
     * Pass in the system command you want to run as a List of Strings, as shown
     * here:
     *
     * List<String> commands = new ArrayList<String>();
     * commands.add("/sbin/ping"); commands.add("-c"); commands.add("5");
     * commands.add("www.google.com"); SystemCommandExecutor commandExecutor =
     * new SystemCommandExecutor(commands); commandExecutor.executeCommand();
     *
     * Note: I've removed the other constructor that was here to support
     * executing the sudo command. I'll add that back in when I get the sudo
     * command working to the point where it won't hang when the given password
     * is wrong.
     *
     * @param commandInformation The command you want to run.
     */
    public SystemCommandExecutor(final List<String> commandInformation) {
        if (commandInformation == null) {
            throw new NullPointerException("The commandInformation is required.");
        }
        this.commandInformation = commandInformation;
        this.adminPassword = null;
        pb = new ProcessBuilder(commandInformation);

    }

    /**
     * Default constructor with no associated commmand.
     */
    public SystemCommandExecutor() {
        pb = new ProcessBuilder("");
        this.adminPassword = null;
    }

    /**
     * Executes the given command with a Process (will wait for the termination)
     *
     * @return @throws IOException
     * @throws InterruptedException
     */
    public int executeCommand()
            throws IOException, InterruptedException {
        if (commandInformation != null) {
            int exitValue = -99;

            try {
                //enviroment variables needed

//            Map<String, String> map = pb.environment();
//            map.put("PATH", Kyoto.constants.getProperty("ixa_bin") + ":/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/");
//            map.put("LD_LIBRARY_PATH", Kyoto.constants.getProperty("ixa_home") + "lib:");
//            map.put("DIRXML_DTD", Kyoto.constants.getProperty("ixa_home") + "share/libixaml");
//            map.put("IXA_PREFIX", Kyoto.constants.getProperty("ixa_home"));
                Process process = pb.start();

                // you need this if you're going to write something to the command's input stream
                // (such as when invoking the 'sudo' command, and it prompts you for a password).
                OutputStream stdOutput = process.getOutputStream();

                // i'm currently doing these on a separate line here in case i need to set them to null
                // to get the threads to stop.
                // see http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
                InputStream inputStream = process.getInputStream();
                InputStream errorStream = process.getErrorStream();

                // these need to run as java threads to get the standard output and error from the command.
                // the inputstream handler gets a reference to our stdOutput in case we need to write
                // something to it, such as with the sudo command
                inputStreamHandler = new ThreadedStreamHandler(inputStream, stdOutput, adminPassword);
                errorStreamHandler = new ThreadedStreamHandler(errorStream);

                // TODO the inputStreamHandler has a nasty side-effect of hanging if the given password is wrong; fix it
                inputStreamHandler.start();
                errorStreamHandler.start();

                // TODO a better way to do this?
                exitValue = process.waitFor();

                // TODO a better way to do this?
                inputStreamHandler.interrupt();
                errorStreamHandler.interrupt();
                inputStreamHandler.join();
                errorStreamHandler.join();
            }
            catch (IOException | InterruptedException e) {
                logger.error("Error while executing the command: ", e);
                throw e;
            }
            finally {
                //we want to know the exit value always
                return exitValue;
            }
        } else {

            throw new NullPointerException("The commandInformation is required.");

        }
    }

    /**
     * Get the standard output (stdout) from the command you just exec'd.
     *
     * @return
     */
    public StringBuilder getStandardOutputFromCommand() {
        return inputStreamHandler.getOutputBuffer();
    }

    /**
     * Get the standard error (stderr) from the command you just exec'd.
     *
     * @return
     */
    public StringBuilder getStandardErrorFromCommand() {
        return errorStreamHandler.getOutputBuffer();
    }
    
    
    /**
     * Pushes a string to the command's  input
     * 
     * @param pString
     */
    
    public void writeToCommand(String pString){
        inputStreamHandler.printWriter.print(pString);
        inputStreamHandler.printWriter.flush();
        
    }

    /**
     * The process with its parameters to be execued
     *
     * @param pCommandInformation
     */
    public void addCommand(final List<String> pCommandInformation) {
        commandInformation = pCommandInformation;
        pb.command(commandInformation);
    }

    /**
     * Adds a environment Variable, not deleting its previous contents ( will
     * add the variable with the ":" prefix
     *
     * @param pKey
     * @param pValue
     */
    public void addEnvironmentVariable(String pKey, String pValue) {
        Map<String, String> map = pb.environment();
        if (map.containsKey(pKey)) {
            //If the variable exits
            String prevValue = map.get(pKey);
            map.remove(pKey);
            map.put(pKey, pValue + ":" + prevValue);
        } else {
            //if doesn't exists just create one
            map.put(pKey, pValue);
        }
    }

    /**
     * Gets the value of given Environment Variable. return nulls if the
     * variable is not defined.
     *
     * @param pKey
     * @return
     */
    public String getEnvironmentVariable(String pKey) {
        Map<String, String> map = pb.environment();
        if (map.containsKey(pKey)) {
            //If the variable exits
            return map.get(pKey);
        } else {
            return null;
        }

    }

    /**
     *
     * @param pWaitTimeSegs - the segs that we will wait until process
     * completion
     * @return @throws IOException
     * @throws InterruptedException
     */
    public int executeCommand(int pWaitTimeSegs)
            throws IOException, InterruptedException {
        if (commandInformation != null) {
            int exitValue = -99;

            try {
                //enviroment variables needed

//            Map<String, String> map = pb.environment();
//            map.put("PATH", Kyoto.constants.getProperty("ixa_bin") + ":/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/");
//            map.put("LD_LIBRARY_PATH", Kyoto.constants.getProperty("ixa_home") + "lib:");
//            map.put("DIRXML_DTD", Kyoto.constants.getProperty("ixa_home") + "share/libixaml");
//            map.put("IXA_PREFIX", Kyoto.constants.getProperty("ixa_home"));
                Process process = pb.start();

                // you need this if you're going to write something to the command's input stream
                // (such as when invoking the 'sudo' command, and it prompts you for a password).
                OutputStream stdOutput = process.getOutputStream();

                // i'm currently doing these on a separate line here in case i need to set them to null
                // to get the threads to stop.
                // see http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
                InputStream inputStream = process.getInputStream();
                InputStream errorStream = process.getErrorStream();

                // these need to run as java threads to get the standard output and error from the command.
                // the inputstream handler gets a reference to our stdOutput in case we need to write
                // something to it, such as with the sudo command
                inputStreamHandler = new ThreadedStreamHandler(inputStream, stdOutput, adminPassword);
                errorStreamHandler = new ThreadedStreamHandler(errorStream);

                // TODO the inputStreamHandler has a nasty side-effect of hanging if the given password is wrong; fix it
                inputStreamHandler.start();
                errorStreamHandler.start();
                Thread.sleep(pWaitTimeSegs * 1000);

                // TODO a better way to do this?
                if (process.isAlive()) {
                    logger.error("Something went wrong, the process has been timed out (timer was " + pWaitTimeSegs + " segs.");
                    inputStreamHandler.interrupt();
                    errorStreamHandler.interrupt();
                    //we do not want to wait as the process has been stalled
                    inputStreamHandler.join(1);
                    errorStreamHandler.join(1);
                    process.destroyForcibly();
                    exitValue = -1;
                } else {
                    exitValue = process.exitValue();

                    // TODO a better way to do this?
                    inputStreamHandler.interrupt();
                    errorStreamHandler.interrupt();
                    inputStreamHandler.join();
                    errorStreamHandler.join();
                }
            }
            catch (IOException | InterruptedException e) {
                logger.error("Error while executing the command: ", e);
                throw e;
            }
            finally {
                //we want to know the exit value always
                return exitValue;
            }
        } else {

            throw new NullPointerException("The commandInformation is required.");

        }
    }

    /**
     * Gets the current Working Directory for this Process
     *
     * @return
     */
    public String getWorkingDirectory() {
        return pb.directory().getAbsolutePath();
    }

    /**
     * Sets the working directory to the directory associated with pFile String
     *
     * @param pFile
     */
    public void setWorkingDirectory(String pFile) {
        pb.directory(new File(pFile));
    }

    public void closeStreams() {

        if (inputStreamHandler != null) {

            if (inputStreamHandler.getPrintWriter() != null) {
                inputStreamHandler.getPrintWriter().close();
            }
        }
        if (errorStreamHandler != null) {
            if (errorStreamHandler.getPrintWriter() != null) {
                errorStreamHandler.getPrintWriter().close();
            }
        }
    }
}
