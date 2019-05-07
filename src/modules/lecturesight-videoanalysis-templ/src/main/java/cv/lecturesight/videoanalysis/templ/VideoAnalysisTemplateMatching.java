package cv.lecturesight.videoanalysis.templ;

import cv.lecturesight.cca.ConnectedComponentLabeler;
import cv.lecturesight.cca.ConnectedComponentService;
import cv.lecturesight.display.DisplayService;
import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.OpenCLService.Format;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.conf.ConfigurationListener;
import cv.lecturesight.util.geometry.Position;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

import lombok.Setter;
import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VideoAnalysisTemplateMatching implements ObjectTracker, ConfigurationListener {

  // -- configuration properties --
  private final static String PROPKEY_CHANGE_THRESH = "change.threshold";
  private final static String PROPKEY_CELL_THRESH = "cell.activation.threshold";
  private final static String PROPKEY_OBJECT_CELLS_MIN = "object.cells.min";
  private final static String PROPKEY_OBJECT_CELLS_MAX = "object.cells.max";
  private final static String PROPKEY_OBJECT_DORMANT_MINTIME = "object.dormant.min";
  private final static String PROPKEY_OBJECT_DORMANT_MAXTIME = "object.dormant.max";
  private final static String PROPKEY_OBJECT_DORMANT_AGE_FACTOR = "object.dormant.age.factor";
  private final static String PROPKEY_OBJECT_MOVE_THRESH = "object.move.threshold";
  private final static String PROPKEY_OBJECT_MATCH_THRESH = "object.match.threshold";
  private final static String PROPKEY_OBJECT_ACTIVE_MINTIME = "object.active.min";
  private final static String PROPKEY_OBJECT_TIMEOUT = "object.timeout";

  // Fraction (denominator) of the proportion of the overview diagonal to regard as a
  // meaningful movement for tracking persistence purposes. Works out to approx 18
  // pixels for 640x360 overview size.
  private final int ORIGIN_MOVE_RATIO = 40;

  @Setter
  Configuration config;       // configuration parameters

  @Setter
  OpenCLService ocl;          // OpenCL service
  OCLSignal sig_START;        // signal triggering processing of new frame
  OCLSignal sig_IMAGEPROC;
  OCLSignal sig_TRACKPREP;
  OCLSignal sig_VA_DONE;

  @Setter
  ConnectedComponentService ccs;
  ConnectedComponentLabeler cclabel;

  @Setter
  DisplayService dsps;        // display service

  @Setter
  FrameSourceProvider fsp;    // service providing the input FrameSource
  FrameSource fsrc;           // input FrameSource

  ComputationRun ocl_main;
  ComputationRun ocl_trackingPrep;
  ComputationRun ocl_trackingUpdate;

  // -- OpenCL Kernels --
  CLKernel abs_diff_thresh_K;
  CLKernel cell_max_K;
  CLKernel visualization_K;
  CLKernel reset_bbox_K;
  CLKernel region_stats_K;
  CLKernel compute_avg_K;
  CLKernel headpos_avg_K;
  CLKernel update_templates_K;
  CLKernel match_templates_K;

  // -- GPU Buffers --
  CLImage2D input_rgb;
  CLImage2D input_rgb_last;
  CLImage2D change;
  CLImage2D cells;
  CLImage2D visual;
  CLImage2D cell_labels;
  CLImage2D templates;
  CLImage2D match;

  // GPU buffers & host arrays for object data
  CLBuffer<Integer> weights_gpu;
  CLBuffer<Integer> centroids_gpu;
  CLBuffer<Integer> bboxes_gpu;
  CLBuffer<Integer> head_data_gpu;
  CLBuffer<Integer> head_pos_gpu;

  int numRegions;
  int[] region_weights;
  int[] region_centroids;
  int[] region_bboxes;
  int[] region_headpos;

  int numTargets = 0;
  int targetSeq = 1;

  Target[] targets;

  int[] updateBuffer;
  IntBuffer updateBuffer_host;
  CLBuffer<Integer> updateBuffer_gpu;

  // kernel launch and buffer parameters
  final int MAX_REGIONS = 36; // maximum number of foreground objects
  final int CELL_SIZE = 8;    // size of cells
  final int TARGET_SIZE = 32;
  final int MAX_TARGETS = 6;
  final int MAX_SAMPLES = 4;
  int[] imageWorkDim;         // work group size of per-pixel kernels
  int[] cellWorkDim;          // work group size of cell kernels
  int[] numCells;             // number of cells in x and y dimension
  int[] templateDim;
  int[] templateBufferDim;
  long templateWorkgroupSize;

  // computation parameters
  int change_threshold;
  int cell_activation_threshold;
  int object_min_cells;
  int object_max_cells;
  int object_dormant_min;
  int object_dormant_max;
  int object_match_threshold;
  int object_timeout;
  float object_dormant_age_factor;
  int object_min_active;
  double object_move_threshold;
  double origin_move_threshold;

  // GPU max workgroup size
  long gpu_maxworkgroupsize;

  /**
   * GPU Main Program implementing the video analysis.
   *
   */
  class DetectionRun implements ComputationRun {

    @Override
    public void launch(CLQueue queue) {

      // Ignore when shutting down
      if (ocl == null)
        return;

      abs_diff_thresh_K.setArgs(input_rgb, input_rgb_last, change, change_threshold);
      abs_diff_thresh_K.enqueueNDRange(queue, imageWorkDim);
      ocl.utils().copyImage(0, 0, imageWorkDim[0], imageWorkDim[1], input_rgb, 0, 0, input_rgb_last);

      cell_max_K.setArgs(change, cells, cell_activation_threshold);
      cell_max_K.enqueueNDRange(queue, imageWorkDim, cellWorkDim);

      visualization_K.setArgs(input_rgb, cells, change, visual);
      visualization_K.enqueueNDRange(queue, imageWorkDim);
    }

    @Override
    public void land() {
      // Ignore when shutting down
      if (ocl == null)
        return;

      ocl.castSignal(sig_IMAGEPROC);
    }
  }

  class TrackingPreparationRun implements ComputationRun {

    IntBuffer weights_host;
    IntBuffer centroids_host;
    IntBuffer headpos_host;
    IntBuffer bboxes_host;

    final int[] regionsDim = {MAX_REGIONS};

    {
      reset_bbox_K.setArgs(bboxes_gpu);
      region_stats_K.setArgs(change, cell_labels, weights_gpu, centroids_gpu, head_data_gpu, bboxes_gpu);
      headpos_avg_K.setArgs(head_data_gpu, head_pos_gpu);
      compute_avg_K.setArgs(centroids_gpu, weights_gpu);
    }

    @Override
    public void launch(CLQueue queue) {

      // Ignore when shutting down
      if (ocl == null)
        return;

      // reset buffers
      ocl.utils().setValues(0, MAX_REGIONS, weights_gpu, 0);
      ocl.utils().setValues(0, MAX_REGIONS * 2, centroids_gpu, 0);
      ocl.utils().setValues(0, MAX_REGIONS * 3, head_data_gpu, Integer.MAX_VALUE);
      reset_bbox_K.enqueueNDRange(queue, regionsDim);

      // compute region statistics
      region_stats_K.enqueueNDRange(queue, imageWorkDim, cellWorkDim);
      compute_avg_K.enqueueNDRange(queue, regionsDim);
      headpos_avg_K.enqueueNDRange(queue, regionsDim);

      // download results
      weights_host = weights_gpu.read(queue);
      centroids_host = centroids_gpu.read(queue);
      headpos_host = head_pos_gpu.read(queue);
      bboxes_host = bboxes_gpu.read(queue);
    }

    @Override
    public void land() {

      // Ignore when shutting down
      if ((ocl == null) || (cclabel == null))
        return;

      numRegions = cclabel.getNumBlobs();

      if (numRegions > 0) {
        weights_host.get(region_weights);
        centroids_host.get(region_centroids);
        headpos_host.get(region_headpos);
        bboxes_host.get(region_bboxes);

        for (int i = 0; i < numRegions; i++) {

          Box changeBox = new Box(region_bboxes[4 * i],
                  region_bboxes[4 * i + 1],
                  region_bboxes[4 * i + 2],
                  region_bboxes[4 * i + 3]);
          Box searchBox = new Box(changeBox.x - 16, changeBox.y - 16, changeBox.max_x + 16, changeBox.max_y + 16);

          List<Target> updated = new LinkedList<Target>();
          for (Target t : targets) {
            if (t != null) {
              if (searchBox.includes(t.x, t.y)) {

                // UPDATE TARGET

                t.updatebox.x = region_centroids[i * 2] - TARGET_SIZE / 2;
                t.updatebox.y = changeBox.y;
                t.updatebox.max_x = t.updatebox.x + TARGET_SIZE;
                t.updatebox.max_y = t.updatebox.y + TARGET_SIZE;
                updated.add(t);
              }
            }
          }

          if (updated.size() > 1) {
            Target oldest = null;
            for (Target u : updated) {
              if (oldest == null || u.id < oldest.id) {
                oldest = u;
              }
            }
            for (Target u : updated) {
              if (oldest.id != u.id) {
                discardTarget(u);
              }
            }
          } else if (updated.isEmpty()) {
            if (changeBox.width() > TARGET_SIZE / 2 && changeBox.height() > TARGET_SIZE / 2) {
              Target new_t = new Target(changeBox.x + changeBox.width() / 2, changeBox.y + TARGET_SIZE / 2);
              int idx = addTarget(new_t);
            }
          }
        }
      }

      ocl.castSignal(sig_TRACKPREP);
    }

  }

  class TrackingUpdateRun implements ComputationRun {

    int numTemplUpdates;
    int numTemplMatches;
    int[] updateDim = new int[]{0, 32};
    IntBuffer output_host;

    @Override
    public void launch(CLQueue queue) {
      // matches templates of targets that already have a template
      numTemplMatches = makeTargetList(updateBuffer, false);
      if (numTemplMatches > 0) {
        ocl.utils().setValues(0, 0, imageWorkDim[0], imageWorkDim[1], match, 0);
        updateDim[0] = TARGET_SIZE * numTemplMatches;
        CLEvent uploaded = updateBuffer_gpu.write(queue, updateBuffer_host, true);
        match_templates_K.setArgs(input_rgb, templates, updateBuffer_gpu);
        CLEvent matched;

        // Workaround for LS-165 (support Intel GPUs).
        // Not totally sure whether this is size-related, or
        // a difference between NVidia and Intel GPUs or OpenCL drivers.

        if (gpu_maxworkgroupsize >= templateWorkgroupSize) {
            matched = match_templates_K.enqueueNDRange(queue, updateDim, templateDim, uploaded);
        } else {
            matched = match_templates_K.enqueueNDRange(queue, updateDim, uploaded);
        }

        output_host = updateBuffer_gpu.read(queue, matched);
      }

      // update templates for all targets
      numTemplUpdates = makeTargetList(updateBuffer, true);
      if (numTemplUpdates > 0) {
        updateDim[0] = TARGET_SIZE * numTemplUpdates;
        CLEvent uploaded = updateBuffer_gpu.write(queue, updateBuffer_host, false);
        update_templates_K.setArgs(input_rgb, cells, templates, updateBuffer_gpu);
        if (gpu_maxworkgroupsize >= templateWorkgroupSize) {
            update_templates_K.enqueueNDRange(queue, updateDim, templateDim, uploaded);
        } else {
            update_templates_K.enqueueNDRange(queue, updateDim, uploaded);
        }
      }
    }

    @Override
    public void land() {

      // Ignore when shutting down
      if (ocl == null)
        return;

      // update targets that were matched
      if (numTemplMatches > 0) {
        int [] results = new int[numTemplMatches * 4];
        output_host.get(results);
        for (int i = 0; i < numTemplMatches; i++) {
          int idx = i * 4;
          int id = results[idx] + 1;    // index --> ID
          updateTarget(id, results[idx + 1], results[idx + 2], results[idx + 3]);
        }
      }

      incrementTargetLifetimes();
      discardInactiveTargets();
      ocl.castSignal(sig_VA_DONE);
    }
  }

  class Target {

    int id = -1;
    int seq = -1;

    // Initial position
    int first_x;
    int first_y;

    // Current position
    int x;
    int y;

    // Offset from last position
    int vx;
    int vy;
    double vt;

    // Max distance moved from point of origin in the target's lifetime
    double vt_origin = 0;

    long first_seen = 0; // Time that the target was first seen
    long time = 0;

    long last_move = 0;  // Time that the target last moved
    long last_match = 0; // Time that the target last matched the template
    int matchscore = 0;  // Last match score

    Box searchbox;
    Box updatebox;

    TrackerObject to;    // TrackerObject repreenting this target

    Target(int x, int y) {
      this.seq = targetSeq++;
      this.x = x;
      this.y = y;
      this.first_x = x;
      this.first_y = y;
      int ht = TARGET_SIZE / 2;
      this.searchbox = new Box(x-ht-5, y-ht-5, x+ht+5, y+ht+5);
      this.updatebox = new Box(x-ht-5, y-ht-5, x+ht+5, y+ht+5);
      first_seen = System.currentTimeMillis();
      last_move = first_seen;
      to = new TrackerObject(first_seen);
    }
  }

  class Box {

    int x;
    int y;
    int max_x;
    int max_y;

    Box(int x, int y, int max_x, int max_y) {
      this.x = x;
      this.y = y;
      this.max_x = max_x;
      this.max_y = max_y;
    }

    public int width() {
      return max_x - x;
    }

    public int height() {
      return max_y - y;
    }

    public boolean includes(int x, int y) {
      return x >= this.x && y >= this.y && x <= max_x && y <= max_y;
    }
  }

  protected void activate(ComponentContext cc) {

    fsrc = fsp.getFrameSource();              // obtain frame source

    sig_START = fsrc.getSignal();             // obtain start signal
    sig_IMAGEPROC = ocl.getSignal("VA_IMAGEPROC");
    sig_TRACKPREP = ocl.getSignal("VA_TRACKPREP");
    sig_VA_DONE = ocl.getSignal("VA_DONE");

    // determine kernel launch & buffer parameters
    imageWorkDim = new int[]{fsrc.getWidth(), fsrc.getHeight()};
    cellWorkDim = new int[]{CELL_SIZE, CELL_SIZE};
    numCells = new int[]{imageWorkDim[0] / CELL_SIZE, imageWorkDim[1] / CELL_SIZE};
    templateDim = new int[]{TARGET_SIZE, TARGET_SIZE};
    templateWorkgroupSize = TARGET_SIZE * TARGET_SIZE;
    templateBufferDim = new int[]{MAX_SAMPLES * TARGET_SIZE, MAX_TARGETS * TARGET_SIZE};

    mapParameters();      // get computation parameters from configuration
    getKernels();         // get kernels
    allocateBuffers();    // allocate GPU buffers
    initBuffers();        // initialize GPU buffers
    registerDisplays();   // register displays

    // register UI
    UserInterface trackerUI = new TrackerUI(this);
    cc.getBundleContext().registerService(UserInterface.class.getName(), trackerUI, null);

    // create connected component labeler working on cell array
    cclabel = ccs.createLabeler(cells, MAX_REGIONS, object_min_cells, object_max_cells);
    cclabel.doLabels(sig_IMAGEPROC);
    cell_labels = cclabel.getLabelImage();

    ocl_trackingPrep = new TrackingPreparationRun();
    ocl.registerLaunch(cclabel.getSignal(ConnectedComponentLabeler.Signal.DONE), ocl_trackingPrep);

    ocl_trackingUpdate = new TrackingUpdateRun();
    ocl.registerLaunch(sig_TRACKPREP, ocl_trackingUpdate);

    ocl_main = new DetectionRun();
    ocl.registerLaunch(sig_START, ocl_main);

    // check the max workgroup size for the selected GPU
    CLDevice[] devices = ocl.context().getDevices();
    for (CLDevice device : devices) {
        gpu_maxworkgroupsize = device.getMaxWorkGroupSize();
        Logger.info("Max workgroup size for OpenCL device: {} {} is {}", device.getVendor(), device.getName(), gpu_maxworkgroupsize);
    }

    // Calculate origin move threshold proportional to overview image size
    origin_move_threshold = Math.sqrt(Math.pow(fsrc.getWidth(), 2) + Math.pow(fsrc.getHeight(), 2)) / ORIGIN_MOVE_RATIO;
    Logger.debug("Calculated origin move threshold is {}", (int) origin_move_threshold);

    Logger.info("Activated.");
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    Logger.info("Deactivated");
  }

  int addTarget(Target t) {
    if (numTargets == MAX_TARGETS) {
      Logger.debug("Maximum number of {} targets exceeded; ignoring additional target", MAX_TARGETS);
      return -1;
    }
    for (int i = 0; i < MAX_TARGETS; i++) {
      if (targets[i] == null) {
        targets[i] = t;
        t.id = i + 1;
        numTargets++;
        return i;
      }
    }
    return -1;
  }

  void discardTarget(Target t) {
    Logger.trace("Discarding target id={}", t.id);
    if (targets[t.id - 1] == t) {
      targets[t.id - 1] = null;
      t.id = -1;
      numTargets--;
    } else {
      throw new IllegalStateException("Target with ID " + t.id + " should be at index " + (t.id - 1) + " but is not!");
    }
  }

  void updateTarget(int id, int x, int y, int match) {

    for (Target t : targets) {
      if (t != null && t.id == id) {

        t.vx = x - t.x;
        t.vy = y - t.y;
        t.vt = Math.sqrt(Math.pow(t.vx, 2) + Math.pow(t.vy, 2));

        Logger.trace("Updating target id={} seq={} time={} last(x,y)={},{} new(x,y)={},{} vt={}  match={}",
                     id, t.seq, t.time, t.x, t.y, x, y, (int) t.vt, match);

        // Set the origin point after a few matches
        if (t.time == 3) {
          t.first_x = x;
          t.first_y = y;
        }

        // Maximum distance moved from point of origin
        if (t.time > 3) {
          double vt_origin = Math.sqrt(Math.pow(x - t.first_x, 2) + Math.pow(y - t.first_y, 2));
          if (vt_origin > t.vt_origin) {
            t.vt_origin = vt_origin;
          }
        }

        // Moved?
        if ((t.vt > object_move_threshold) && (match > object_match_threshold)) {
          t.last_move = System.currentTimeMillis();
        }

        // Positive match?
        if (match > object_match_threshold) {
          t.last_match = System.currentTimeMillis();
        }

        t.x = x;
        t.y = y;
        t.matchscore = match;

        // Update the search box
        int ht = TARGET_SIZE / 2;
        t.searchbox.x = x-ht-5;
        t.searchbox.y = y-ht-5;
        t.searchbox.max_x = x+ht+5;
        t.searchbox.max_y = y+ht+5;

        // Amount to expand search box by relative to movement. Lateral movement more likely.
        double vxfactor = 2.5;
        double vyfactor = 1.5;

        if (t.vx < 0) t.searchbox.x += vxfactor*t.vx;
        if (t.vx > 0) t.searchbox.max_x += vxfactor*t.vx;

        if (t.vy < 0) t.searchbox.y += vyfactor*t.vy;
        if (t.vy > 0) t.searchbox.max_y += vyfactor*t.vy;

        updateTrackerObject(t);

        break;
      }
    }
  }

  void incrementTargetLifetimes() {
    for (Target t : targets) {
      if (t != null) {
        t.time++;
      }
    }
  }

  /**
   * Discard targets which have been inactive for between the min and max dormant limits.
   * Longer-lived objects are allowed to be dormant for longer before being discarded.
   */
  void discardInactiveTargets() {

    long now = System.currentTimeMillis();
    for (Target t : targets) {
      if (t != null) {
         long target_age = now - t.first_seen;
         long target_dormant = now - t.last_move;
         int dormant_scaled = Math.min(object_dormant_max, object_dormant_min + Math.max(Math.round(object_dormant_age_factor * (target_age - object_dormant_min)),0));

         // If the object has moved since first tracked, assume it is worth following and make the timeout much higher
         if ((object_timeout > dormant_scaled) && (t.vt_origin > origin_move_threshold) && (t.matchscore > object_match_threshold)) {
           dormant_scaled = object_timeout;
         }

         Logger.trace("Checking target target seq={} id={} vt_origin={} matchscore={} age={} dormant={} dormant_scaled={}",
                       t.seq, t.id, (int) t.vt_origin, t.matchscore, target_age, target_dormant, dormant_scaled);

         if (target_dormant > dormant_scaled) {
           Logger.debug("Discarding target seq={} id={} vt_origin={} matchscore={} age={} dormant={} dormant_scaled={}",
                         t.seq, t.id, (int) t.vt_origin, t.matchscore, target_age, target_dormant, dormant_scaled);
           discardTarget(t);
         }
      }
    }
  }

  int makeTargetList(int[] a, boolean filterTargets) {
    int num = 0;
    int j = 0;
    for (int i = 0; i < MAX_TARGETS; i++) {

      // try to find next target that needs update
      Target next = null;
      for (; j < MAX_TARGETS; j++) {
        Target t = targets[j];

        if (t != null) {
          if (filterTargets && t.time > 0) {
            next = targets[j++];
            break;
          } else {
              next = targets[j++];
              break;
          }
        }
      }

      // if next target was found, put coords into update list
      if (next != null) {
        num++;
        int idx = i * 4;
        a[idx] = next.id - 1;  // ID --> index !
        a[idx + 1] = next.updatebox.x+16;
        a[idx + 2] = next.updatebox.y+16;
        a[idx + 3] = 0;
      }

      if (j + 1 == MAX_TARGETS) {
        break;
      }
    }
    return num;
  }

  /**
   * Gets computation parameters from the configuration.
   *
   */
  private void mapParameters() {
    change_threshold = config.getInt(PROPKEY_CHANGE_THRESH);
    cell_activation_threshold = config.getInt(PROPKEY_CELL_THRESH);
    object_min_cells = config.getInt(PROPKEY_OBJECT_CELLS_MIN);
    object_max_cells = config.getInt(PROPKEY_OBJECT_CELLS_MAX);
    object_dormant_min = config.getInt(PROPKEY_OBJECT_DORMANT_MINTIME);
    object_dormant_max = config.getInt(PROPKEY_OBJECT_DORMANT_MAXTIME);
    object_dormant_age_factor = config.getFloat(PROPKEY_OBJECT_DORMANT_AGE_FACTOR);
    object_min_active = config.getInt(PROPKEY_OBJECT_ACTIVE_MINTIME);
    object_move_threshold = config.getDouble(PROPKEY_OBJECT_MOVE_THRESH);
    object_match_threshold = config.getInt(PROPKEY_OBJECT_MATCH_THRESH);
    object_timeout = config.getInt(PROPKEY_OBJECT_TIMEOUT);
  }

  /**
   * Obtains OpenCL kernels from OpenCL service.
   *
   */
  private void getKernels() {
    abs_diff_thresh_K = ocl.programs().getKernel("util", "abs_diff_thresh");
    cell_max_K = ocl.programs().getKernel("cells", "cells_max_8");
    visualization_K = ocl.programs().getKernel("cells", "viz_cells");
    region_stats_K = ocl.programs().getKernel("foreground", "compute_object_stats_8");
    reset_bbox_K = ocl.programs().getKernel("foreground", "reset_bbox_buffer");
    compute_avg_K = ocl.programs().getKernel("foreground", "compute_average");
    headpos_avg_K = ocl.programs().getKernel("foreground", "avg_headpos");
    update_templates_K = ocl.programs().getKernel("template", "update_templates");
    match_templates_K = ocl.programs().getKernel("template", "match_templates");
  }

  /**
   * Allocates GPU buffers.
   *
   * @throws IllegalStateException
   */
  private void allocateBuffers() throws IllegalStateException {
    // obtain buffers from FrameSource
    input_rgb = fsrc.getImage();
    input_rgb_last = fsrc.getLastImage();

    // allocate working buffers
    change = allocImage2D(Format.RGBA_UINT8, imageWorkDim);
    cells = allocImage2D(Format.RGBA_UINT8, numCells);
    visual = allocImage2D(Format.RGBA_UINT8, imageWorkDim);

    targets = new Target[MAX_TARGETS];
    updateBuffer = new int[MAX_TARGETS * 4];
    updateBuffer_host = IntBuffer.wrap(updateBuffer);
    updateBuffer_gpu = ocl.context().createBuffer(CLMem.Usage.InputOutput, updateBuffer_host);

    templates = allocImage2D(Format.RGBA_UINT8, templateBufferDim);
    match = allocImage2D(Format.RGBA_UINT8, imageWorkDim);

    // allocate buffers and arrays for object data
    weights_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS);
    centroids_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS * 2);
    bboxes_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS * 4);
    head_data_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS * 3);
    head_pos_gpu = ocl.context().createIntBuffer(CLMem.Usage.InputOutput, MAX_REGIONS);

    region_weights = new int[MAX_REGIONS];
    region_centroids = new int[MAX_REGIONS * 2];
    region_bboxes = new int[MAX_REGIONS * 4];
    region_headpos = new int[MAX_REGIONS];
  }

  private CLImage2D allocImage2D(Format format, int[] dim) {
    return ocl.context().createImage2D(CLMem.Usage.InputOutput, format.getCLImageFormat(), dim[0], dim[1]);
  }

  /**
   * Initializes GPU buffers where necessary.
   *
   */
  private void initBuffers() {
    ocl.utils().setValues(0, 0, templateBufferDim[0], templateBufferDim[1], templates, 0);
  }

  private void registerDisplays() {
    dsps.registerDisplay("visual", visual, sig_VA_DONE);
    dsps.registerDisplay("change", change, sig_VA_DONE);
    dsps.registerDisplay("templates", templates, sig_VA_DONE);
    dsps.registerDisplay("cells", cells, sig_VA_DONE);
  }

  @Override
  public void configurationChanged() {
    if (change_threshold != config.getInt(PROPKEY_CHANGE_THRESH)) {
      change_threshold = config.getInt(PROPKEY_CHANGE_THRESH);
      Logger.info("Setting change threshold to {}", change_threshold);
    }

    if (cell_activation_threshold != config.getInt(PROPKEY_CELL_THRESH)) {
      cell_activation_threshold = config.getInt(PROPKEY_CELL_THRESH);
      Logger.info("Setting cell activation threshold {}" , cell_activation_threshold);
    }

    if (object_min_cells != config.getInt(PROPKEY_OBJECT_CELLS_MIN)) {
      object_min_cells = config.getInt(PROPKEY_OBJECT_CELLS_MIN);
      Logger.info("Setting min number of cells in objects to {}", object_min_cells);
    }

    if (object_max_cells != config.getInt(PROPKEY_OBJECT_CELLS_MAX)) {
      object_max_cells = config.getInt(PROPKEY_OBJECT_CELLS_MAX);
      Logger.info("Setting max number of cells in objects to {}", object_max_cells);
    }

    if (object_dormant_min != config.getInt(PROPKEY_OBJECT_DORMANT_MINTIME)) {
      object_dormant_min = config.getInt(PROPKEY_OBJECT_DORMANT_MINTIME);
      Logger.info("Setting min time in milliseconds before discarding a target to {} ms", object_dormant_min);
    }

    if (object_dormant_max != config.getInt(PROPKEY_OBJECT_DORMANT_MAXTIME)) {
      object_dormant_max = config.getInt(PROPKEY_OBJECT_DORMANT_MAXTIME);
      Logger.info("Setting max time in milliseconds before discarding a target to {} ms", object_dormant_max);
    }

    if (object_dormant_age_factor != config.getFloat(PROPKEY_OBJECT_DORMANT_AGE_FACTOR)) {
      object_dormant_age_factor = config.getFloat(PROPKEY_OBJECT_DORMANT_AGE_FACTOR);
      Logger.info("Setting age factor for discarding a target to {}", object_dormant_age_factor);
    }

    if (object_min_active != config.getInt(PROPKEY_OBJECT_ACTIVE_MINTIME)) {
      object_min_active = config.getInt(PROPKEY_OBJECT_ACTIVE_MINTIME);
      Logger.info("Setting minimum time in milliseconds before an object is recognized for tracking to {} ms", object_min_active);
    }

    if (object_move_threshold != config.getDouble(PROPKEY_OBJECT_MOVE_THRESH)) {
      object_move_threshold = config.getDouble(PROPKEY_OBJECT_MOVE_THRESH);
      Logger.info("Setting target movement threshold to {}", object_move_threshold);
    }

    if (object_match_threshold != config.getInt(PROPKEY_OBJECT_MATCH_THRESH)) {
      object_match_threshold = config.getInt(PROPKEY_OBJECT_MATCH_THRESH);
      Logger.info("Setting target template match threshold to {}", object_match_threshold);
    }

    if (object_timeout != config.getInt(PROPKEY_OBJECT_TIMEOUT)) {
      object_timeout = config.getInt(PROPKEY_OBJECT_TIMEOUT);
      Logger.info("Setting target timeout to {} ms", object_timeout);
    }
  }

  // Tracker methods ___________________________________________________________
  @Override
  public OCLSignal getSignal() {
    return sig_VA_DONE;
  }

  @Override
  public TrackerObject getObject(int id) {
    for (Target t : targets) {
      if (t != null && t.id == id) {
        return t.to;
      }
    }
    return null;
  }

  @Override
  public boolean isCurrentlyTracked(TrackerObject object) {
    for (Target t : targets) {
      if (t != null && t.id == object.getId()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void discardObject(TrackerObject object) {
    for (Target t : targets) {
      if (t != null && t.id == object.getId()) {
        discardTarget(t);
      }
    }
  }

  @Override
  public Map<Integer, TrackerObject> getAllObjects() {
    Map m = new HashMap<Integer, TrackerObject>();
    for (Target t : targets) {
      if (t != null) {
        m.put(t.id, t.to);
      }
    }
    return m;
  }

  @Override
  public List<TrackerObject> getCurrentlyTracked() {
    // return trackable objects which are older than object_min_active milliseconds
    long seen_before = System.currentTimeMillis() - object_min_active;
    List l = new LinkedList<TrackerObject>();
    for (Target t : targets) {
      if ((t != null) && (t.first_seen < seen_before)) {
        l.add(t.to);
      }
    }
    return l;
  }

  void updateTrackerObject(Target t) {
    t.to.setId(t.seq);
    if (t.last_match > t.last_move) {
      t.to.setLastSeen(t.last_match);
    } else {
      t.to.setLastSeen(t.last_move);
    }
    t.to.setProperty(ObjectTracker.OBJ_PROPKEY_CENTROID, new Position(t.x, t.y));
  }

  // Console Commands __________________________________________________________
  public void reset(String[] args) {
    for (int i = 0; i < MAX_TARGETS; i++) {
      targets[i] = null;
    }
    numTargets = 0;
    Logger.info("Clearing target list.");
  }
}
