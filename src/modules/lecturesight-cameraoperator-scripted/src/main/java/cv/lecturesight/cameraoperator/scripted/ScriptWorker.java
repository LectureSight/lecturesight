package cv.lecturesight.cameraoperator.scripted;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.pmw.tinylog.Logger;

public class ScriptWorker implements Runnable {

  String name;         // name used for the script logger
  Scriptable scope;    // scope the script is running in
  
  List<String> imports;        // list of imports that are added to script space before the script is loaded
  Map<String,Object> objects;  // list of Java object that are added to script space before script is loaded

  // queue that stores function calls to be made
  BlockingQueue<Invocation> invocationQueue = new LinkedBlockingQueue<Invocation>();

  private boolean running = true;   // indicates if this worker shuold still run
  private boolean stopped = false;  // indicates if this worker is still running

  /**
   * Constructor, prepares the script for function invocation.
   *
   * @param source File holding the script's source code
   * @throws Exception
   */
  public ScriptWorker(String name) throws Exception {
    this.name = name;
    imports = new LinkedList<String>();
    
    // initialize script scope
    Context ctx = Context.enter();
    scope = ctx.initStandardObjects(null, true);
    Context.exit();
  }

  /**
   * Run method implementing main loop of this worker. While <code>running ==
   * true</code> and not being interrupted an <code>Invocation</code> is taken
   * from the queue and executed. Before ending, <code>stopped</code> is set to
   * <code>true</code> to indicate that this worker was gracefully stopped.
   *
   * If an error is encountered during execution of an invocation, the error is
   * reported to the logger but the worker is not shut down, so that subsequent
   * invocations can still be executed (errors don't completely break the
   * script).
   */
  @Override
  public void run() {
    Context ctx = Context.enter();
    try {
      while (running) {
        Invocation inv = invocationQueue.take();
        try {
          Object ref;
          if (inv instanceof InvocationByName) {
            ref = scope.get(((InvocationByName)inv).getName(), scope);
          } else {
            ref = ((InvocationByReference)inv).getReference();
          }
          if (!(ref instanceof Function)) {
            Logger.error("Invocation error: function not found.");
          } else {
            Function func = (Function) ref;
            func.call(ctx, scope, scope, inv.args);
          }
        } catch (Exception e) {
          Logger.error("Error: " + e.getMessage(), e);
        }
      }
      Logger.info("Interpreter exiting.");
    } catch (InterruptedException ie) {
      Logger.info("Interrupted while waiting for invocations. Interpreter exiting.");
    } finally {
      Context.exit();
      stopped = true;
    }
  }

  /**
   * Makes the interpreter import the Java package specified by
   * <code>package</code>.
   *
   * @param packageName
   */
  public void addImport(String packageName) {
    Context ctx = Context.enter();
    try {
      StringBuilder code = new StringBuilder();
      code.append("importPackage(Packages.").append(packageName).append(");");
      ctx.evaluateString(scope, code.toString(), "scriptWorker", 1, null);
    } catch (Exception e) {
      Logger.error("Failed to import package " + packageName , e);
    } finally {
      Context.exit();
    }
  }

  /**
   * Adds an object <code>obj</code> to the list of objects that are loaded into 
   * script space when load() is called.
   *
   * @param name
   * @param obj
   */
  public void addScriptObject(String name, Object obj) {
    Logger.info("Adding object \"" + name + "\" to script scope.");
    Context.enter();
    try {
      Object wrapped = Context.javaToJS(obj, scope);
      ScriptableObject.putProperty(scope, name, wrapped);
    } catch (Exception e) {
      Logger.error("Failed to add script object " + name, e);
    } finally {
      Context.exit();
    }
  }
  
  /** Loads and evaluates the specified <code>scriptfile</code>. 
   * 
   * @param scriptfile 
   */
  public void load(File scriptfile) {
    Logger.info("Loading script " + scriptfile.getName());
    Context ctx = Context.enter();
    try {
      ctx.evaluateReader(scope, new FileReader(scriptfile), this.name, 1, null);
    } catch (FileNotFoundException e) {
      String msg = "Source file not found: " + scriptfile.getAbsolutePath();
      throw new IllegalStateException(msg, e);
    } catch (IOException e) {
      String msg = "Failed reading source: " + scriptfile.getAbsolutePath();
      Logger.error(msg, e);
    } catch (IllegalStateException e) {
      Logger.error("Error while evaluating script.", e);
      throw e;
    } finally {
      Context.exit();
    }
  }

  /**
   * Will execute the specified <code>function</code> with the specified
   * <code>args</code>. This method is intended only for invoking callbacks from
   * inside a script call to a bridge function.
   *
   * @param func
   * @param args
   */
  public void invokeCallback(Object func, Object... args) {
    Context ctx = Context.enter();       // TODO aren't we still in the context?
    try {
      Function function = (Function) func;
      function.call(ctx, scope, scope, args);
    } catch (Exception e) {
      Logger.error("Exception while calling script function. ", e);
    } finally {
      Context.exit();
    }
  }

  /**
   * Creates and enqueues an <code>Invocation</code> of the JS function with the
   * specified name and the specified parameter list.
   *
   * @param function
   * @param args
   */
  public void invokeMethod(String function, Object... args) {
    Logger.debug("Submitting invocation of function " + function + " with " + args.length + " parameters.");
    Invocation inv = new InvocationByName(function, args);
    invocationQueue.add(inv);
  }

  /**
   * Creates and enqueues an <code>Invocation</code> of the JS function with the
   * specified name and the specified parameter list.
   *
   * @param function
   * @param args
   */
  public void invokeMethod(Object function, Object... args) {
    Logger.debug("Submitting invocation of function reference with " + args.length + " parameters.");
    Invocation inv = new InvocationByReference(function, args);
    invocationQueue.add(inv);
  }

  /**
   * Creates the JS code of the <code>Config</code> object, creates the object
   * and injects it into the script scope.
   *
   * @param props Configuration Properties
   */
  void setScriptConfig(Properties props) {
    // make JS code of config object
    List<String> params = new ArrayList<String>();
    for (Entry<Object, Object> entry : props.entrySet()) {
      String key = (String) entry.getKey();         // TODO sanitize key string
      String val = (String) entry.getValue();       // TODO sanitize value string
      params.add("\"" + key + "\":\"" + val + "\"");
    }
    String code = "var Config = {" + join(params.iterator(), ",") + "};";

    // inject Config object 
    Context ctx = Context.enter();
    try {
      ctx.evaluateString(scope, code, "<scriptWorker>", 1, null);
    } catch (Exception e) {
      Logger.error("Failed to inject configuration object into script. ", e);
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

  /**
   * Requests this worker to stop.
   *
   */
  public void stop() {
    running = false;
  }

  /**
   * Returns true iff this worker has stopped gracefully.
   *
   * @return ture, iff this worker has stopped
   */
  public boolean isStopped() {
    return stopped;
  }
}
