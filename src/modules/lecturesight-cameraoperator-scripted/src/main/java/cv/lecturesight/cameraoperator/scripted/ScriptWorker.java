package cv.lecturesight.cameraoperator.scripted;

import cv.lecturesight.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class ScriptWorker implements Runnable {

  Log log = new Log("Script");  // our logger

  ScriptEngine engine;    // scripting engine
  Invocable script;       // scripting engine as Invocable

  // queue that stores function calls to be made
  BlockingQueue<Invocation> invokeQueue = new LinkedBlockingQueue<Invocation>();

  private boolean running = true;   // indicates if this worker shuold still run
  private boolean stopped = false;  // indicates if this worker is still running

  /** Constructor, prepares the script for function invocation.
   * 
   * @param source
   * @param engine
   * @throws Exception 
   */
  public ScriptWorker(File source, ScriptEngine engine) throws Exception {
    this.engine = engine;
    script = (Invocable) engine;
    try {
        engine.eval(new FileReader(source));    // evaluate script so that all definitions are loaded
    } catch (FileNotFoundException e) {
      log.error("Unable to read script source.", e);
      throw e;
    } catch (ScriptException e) {
      log.error("Error while evaluating script.", e);
      throw e;
    }
  }

  /** Run method implementing main loop of this worker. While <code>running == 
   * true</code> and not being interrupted an <code>Invocation</code> is taken
   * from the queue and executed. Before ending, <code>stopped</code> is set to
   * <code>true</code> to indicate that this worker was gracefully stopped.
   * 
   * If an error is encountered during execution of an invocation, the error is
   * reported to the logger but the worker is not shut down, so that subsequent
   * invocations can still be executed (errors don't completely break the script).
   */
  @Override
  public void run() {
    try {
      while (running) {
        Invocation inv = invokeQueue.take();
        try {
          script.invokeFunction(inv.function, inv.args);
        } catch (NoSuchMethodException e) {
          log.warn("Invocation error: " + e.getMessage());
        } catch (ScriptException e) {
          log.warn("Script error: " + e.getMessage());
        }
      }
      log.warn("Interpreter exiting.");
    } catch (InterruptedException ie) {
      log.warn("Interrupted while waiting for invocations. Interpreter exiting.");
    }
    stopped = true;
  }

  /** Enqueues a new <code>Invocation</code> that will execute the specified 
   * <code>function</code>. 
   * 
   * @param function
   * @param args 
   */ 
  public void invoke(String function, Object... args) {
    log.info("Invoking " + function + " with " + args.length + " parameters.");
    Invocation inv = new Invocation(function, args);
    invokeQueue.add(inv);
  }

  /** Injects an object <code>obj</code> into the script space under the 
   * specified <code>name</code> as identifier.
   * 
   * @param name
   * @param obj 
   */
  public void injectObject(String name, Object obj) {
    engine.put(name, obj);
  }

  /** Makes the interpreter import the Java package specified by 
   * <code>package</code>.
   * 
   * @param packageName 
   */
  public void injectPackage(String packageName) {
    try {
      StringBuilder code = new StringBuilder();
      code.append("importPackage(Packages.").append(packageName).append(");");
      engine.eval(code.toString());
    } catch (ScriptException e) {
      log.error("Unable to import package " + packageName + " in script. ", e);
    }
  }

  /** Requests this worker to stop.
   * 
   */
  public void stop() {
    running = false;
  }
  
  /** Returns true iff this worker has stopped gracefully.
   * 
   * @return ture, if this worker has stopped 
   */ 
  public boolean isStopped() {
    return stopped;
  }
}
