package cv.lecturesight.cameraoperator.scripted;

import cv.lecturesight.scripting.api.ScriptParent;
import cv.lecturesight.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ScriptWorker implements ScriptParent, Runnable {

  Log log = new Log("Script");  // our logger

  String filename;     // name of the js file that this worker executes
  
  Scriptable scope;
  
  // queue that stores function calls to be made
  BlockingQueue<Invocation> invokeQueue = new LinkedBlockingQueue<Invocation>();

  private boolean running = true;   // indicates if this worker shuold still run
  private boolean stopped = false;  // indicates if this worker is still running

  /** Constructor, prepares the script for function invocation.
   * 
   * @param source File holding the script's source code
   * @throws Exception 
   */
  public ScriptWorker(File source) throws Exception {
    Context ctx = Context.enter();                                // TODO refine this
    scope = ctx.initStandardObjects(null, true);
    filename = source.getName();
    try {
      ctx.evaluateReader(scope, new FileReader(source), filename, 1, null); // why doesn't this throw anything in case of compilation error?
    } catch (FileNotFoundException e) {
      log.error("Source file not found.", e);
      throw e;
    } catch (IOException e) {
      log.error("Failed reading source file.", e);
      throw e;
    } finally {
      Context.exit();
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
    Context ctx = Context.enter();
    try {
      while (running) {
        Invocation inv = invokeQueue.take();
        try {
          Object o = scope.get(inv.function, scope);
          if (!(o instanceof Function)) {
            log.error("Invocation error: function " + inv.function + " not found.");
          } else {
            Function func = (Function)o;
            func.call(ctx, scope, scope, inv.args);
          }
        } catch (Exception e) {
          log.error("Error: " + e.getMessage(), e);
        }
      }
      log.info("Interpreter exiting.");
    } catch (InterruptedException ie) {
      log.info("Interrupted while waiting for invocations. Interpreter exiting.");
    } finally {
      Context.exit();
      stopped = true;
    }
  }

  /** Enqueues a new <code>Invocation</code> that will execute the specified 
   * <code>function</code>. This method is intended only for invocing callbacks
   * from inside a script call to a bridge function.
   * 
   * @param func
   * @param args 
   */ 
  public void invokeCallback(Object func, Object... args) {
    Context ctx = Context.enter();
    Function function = (Function)func;
    function.call(ctx, scope, scope, args);  
  }

  /** Injects an object <code>obj</code> into the script space under the 
   * specified <code>name</code> as identifier.
   * 
   * @param name
   * @param obj 
   */
  public void injectObject(String name, Object obj) {
    Object wrapped = Context.javaToJS(obj, scope);
    ScriptableObject.putProperty(scope, name, wrapped);
  }

  /** Makes the interpreter import the Java package specified by 
   * <code>package</code>.
   * 
   * @param packageName 
   */
  public void injectPackage(String packageName) {
    Context ctx = Context.enter();
    try {
      StringBuilder code = new StringBuilder();
      code.append("importPackage(Packages.").append(packageName).append(");");
      ctx.evaluateString(scope, code.toString(), "<scriptWorker>", 1, null);
    } catch (Exception e) {
      log.error("Unable to import package " + packageName + " in script. ", e);
    } finally {
      Context.exit();
    }
  }
  
  /** Creates the JS code of the <code>Config</code> object, creates the object
   * and injects it into the script scope.
   * 
   * @param props Configuration Properties
   */
  void setScriptConfig(Properties props) {
    // make JS code of config object
    List<String> params = new ArrayList<String>();
    for (Entry<Object,Object> entry : props.entrySet()) {
      String key = (String)entry.getKey();         // TODO sanitize key string
      String val = (String)entry.getValue();       // TODO sanitize value string
      params.add("\"" + key + "\":\"" + val + "\"");
    }
    String code = "var Config = {" + join(params.iterator(), ",") + "};";
    
    // inject Config object 
    Context ctx = Context.enter();
    try {
      ctx.evaluateString(scope, code, "<scriptWorker>", 1, null);
    } catch (Exception e) {
      log.error("Failed to inject configuration object into script. ", e);
    } finally {
      Context.exit();
    }
  }
  
  private String join(Iterator<String> it, String sep) {
    StringBuilder sb = new StringBuilder();
    while (it.hasNext()) {
      String s = it.next();
      sb.append(s);
      if (it.hasNext()) {
        sb.append(sep);
      }
    }
    return sb.toString();
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

  /** Invokes the JS function with the specified name and the specified parameter
   * list. 
   * 
   * @param function
   * @param args 
   */
  @Override
  public void invokeMethod(String function, Object... args) {
    log.debug("Submitting invocation of function " + function + " with " + args.length + " parameters.");
    Invocation inv = new Invocation(function, args);
    invokeQueue.add(inv);
  }

  /** Invokes the JS function with the specified name and the specified parameter
   * list and returns the functions return value as a Java object.
   * 
   * @param name
   * @param args
   * @return 
   */
  @Override
  public Object invokeFunction(String name, Object... args) {
    return null;
  }
  
  @Override
  public Log getLogger() {
    return log;
  }
}
